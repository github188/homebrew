# -*- coding: utf-8 -*-

import sys
import os
import glob
import string
# import Image
import httplib
import base64
import urlparse
import urllib

from sgmllib import SGMLParser



# 自动切白边，改变尺寸
#
# 参数
#   fname: 文件名
#   portrait: 肖像模式。置为真时，将风景模式旋转为肖像模式
# 返回值
#   无
def page_auto_crop(fname, portrait=False):

    out = fname[:string.rfind(fname, '.')] + '.png'
    dbg = False # 打开调试
    comic_type = 3

    # 无效颜色
    white = (220, 220, 220) # r,g,b

    bw = False # 灰度图
    im = Image.open(fname)
    if im.mode == 'L' or im.mode == 'P':
        im = im.convert('RGB')
        bw = True
    elif im.mode == 'RGBA':
        im = im.convert('RGB')
    elif im.mode == 'RGB':
        pass
    else:
        print 'image mode', im.mode, 'not support!'
        return
    
    w = im.size[0]
    h = im.size[1]

    print 'Processing...', fname
    print '  width=%d height=%d' % (w, h)
    
    # 当设置 portrait 为 true 时，将横向图片顺时针旋转90度
    if portrait and w > h:
        im = im.rotate(90)
        w = im.size[0]
        h = im.size[1]
        print '  rotate %d x %d' % (w, h)
        
    data = im.getdata()

    # 剪裁位置
    x1 = 0
    x2 = w
    y1 = 0
    y2 = h

    scan = 20 # 在首次扫描到有效位置后，再进行的试探扫描量

    # 水平扫描范围限制
    limit = w / 5 # 仅扫描图片的1/5宽度
    valid = h * 0.02 # 取高度的 2% 作为噪点
    if dbg:
        print '  horizontal-scanning-limit=%d valid=%d' % (limit, valid)
    
    # 扫描左边最大白边
    i = 0
    ci = -1 # 记录上次的切边位置
    cc = 0 # 有效扫描次数
    for i in range(0, limit):
        j = 0
        count = 0 # 有效像素
        pos = i
        for j in range(0, h):
            if data[pos] < white:
                count += 1
            pos += w
                
            if count > valid:
                break

        # 检测列扫描结果            
        if count > valid:
            cc += 1
        if count > valid and ci == -1:
            ci = i
        if count < valid:
            cc = 0
            ci = -1
        if dbg:
            print '    %3d: %3d %3d %3d' % (i, count, cc, ci)
        if cc >= scan:
            i = ci
            break
            
    if dbg:
        print '  left side crop at X:', i
    x1 = i

    # 扫描右边最大白边
    i = 0
    ci = -1
    cc = 0
    for i in range(1, limit + 1):
        j = 0
        count = 0
        pos = w - i
        found = False
        for j in range(0, h):
            if data[pos] < white:
                count += 1
            pos += w

            if count > valid:
                break

        if count > valid:
            cc += 1
        if count > valid and ci == -1:
            ci = i
        if count < valid:
            cc = 0
            ci = -1
        if dbg:
            print '    %3d: %3d %3d %3d' % (w-i, count, cc, ci)
        if cc >= scan:
            i = ci
            break
        
    i = w - i + 1
    if dbg:
        print '  right side crop at X:', i
    x2 = i

    # 垂直扫描范围限制
    limit = h / 6 # 图片高度的 1/6
    valid = w * 0.01 # 宽度的 1%
    if dbg:
        print '  vertical-scanning-limit=%d valid=%d' % (limit, valid)

    # 扫描顶部最大白边
    i = 0
    for i in range(0, limit):
        j = 0
        count = 0
        pos = i * w
        found = False
        for j in range(0, w):
            if data[pos + j] < white:
                count += 1
            if count > valid:
                found = True
                break
        if found:
            break
    if dbg:
        print '  top side crop at Y:', i
    y1 = i
            
    # 扫描底部最大白边
    i = 0
    for i in range(1, limit + 1):
        j = 0
        count = 0
        pos = (h - i) * w
        found = False
        for j in range(0, w):
            if data[pos + j] < white:
                count += 1
            if count > valid:
                found = True
                break
        if found:
            break
    i = h - i + 1    
    if dbg:
        print '  bottom side crop at Y:', i
    y2 = i

    box = (x1, y1, x2, y2)
    im = im.crop(box)
    print '  crop-box', box, ' gray:', bw
    if bw:
        im = im.convert('L')

    if dbg:
        print '  save to test.png'
        im.save('/temp/test.png')
        return
    

    # 改变尺寸
    t = comic_type

    if t == 1:    
        # 日本漫画 K5
        max_w = 480
        max_h = 800
    elif t == 3:
        max_w = 600
        max_h = 1000
    elif t == 4:
        max_w = 600
        max_h = 760
    elif t == 2:
        # 欧美漫画
        max_w = 780
        max_h = 1300
    else:
        # 更高
        max_w = 900
        max_h = 1500
        
    # 长宽比
    wr = 3
    hr = 5

    # 当图片小于指定区域时，不放大
    w = im.size[0]
    h = im.size[1]
    if w < 100 or h < 100:
        print '  Error!!!', w, 'x', h
        return
    if w <= max_w and h <= max_h:
        print '  old size', w, 'x', h
        im.save(out)
        return

    # 以宽度为准计算缩放高度
    ch = w / wr * hr
    if ch >= h:
        # 宽度撑满，改变高度
        nw = max_w
        nh = h * nw / w
    else:
        # 高度撑满，改变宽度
        nh = max_h
        nw = w * nh / h

    print '  new size', nw, 'x', nh
    im = im.resize((nw, nh), Image.ANTIALIAS)
    im.save(out)
    


# 对双页图片自动拆分
#
# 参数
#   fname: 文件名
#   pno: 编号, 双页时，编号自动加一
#   left: True 从左到右计数；False 从右到左计数
#   crop: 固定切边宽度，左页切左边，右页切右边
# 返回值
#   true: 分开页面
#   false: 不分开
def split_page(fname, pno, left=True, crop=0):
    path = 'temp/'
    im = Image.open(fname)
    w = im.size[0]
    h = im.size[1]
    print 'Processing...', fname
    print '  width=%d height=%d crop=%d' % (w, h, crop)
    name = ''
    if w < h:
        # 单页
        name = '%sp%03d.png' % (path, pno)
        print '  =>', name
        im.save(name)
        
        return False
    
    else:
        # 双页
        nw = w / 2
        w = nw * 2 # 左右页等宽

        # 检测有效分页位置
        white = (220, 220, 200)
        pw = im.size[0]

        ####################
        # 转换至 RGB 模式
        bw = False
        if im.mode == 'L' or im.mode == 'P':
            im = im.convert('RGB')
            bw = True
        elif im.mode == 'RGBA':
            im = im.convert('RGB')
        elif im.mode == 'RGB':
            pass
        else:
            print 'image mode', im.mode, 'not support!'
            return
        
        # 向左检测分页位置
        found = False
        limit = int(nw * 0.2)
        print '  mid=%d limit=%d' % (nw, limit)
        data = im.getdata()
        for i in range(1, limit + 1):
            pos = nw - i
            count = 0
            for j in range(0, h):
                if data[pos] < white:
                    count += 1
                pos += pw

##            print nw - i, count
            if count == 0:
                found = True
                break
            
        if found:
            nw = nw - i + 1
            print '  left offset', nw - i + 1
            found = False
        else:
##            print '-----'
            # 向右检测分页位置
            for i in range(limit):
                pos = nw + i
                count = 0
                for j in range(0, h):
                    if data[pos] < white:
                        count += 1
                    pos += pw

##                print nw + i, count
                if count == 0:
                    found = True
                    break
                
        if found:
            nw = nw + i
            print '  right offset', nw
            
        if bw:
            im = im.convert('L')

        # 取左边页
        box = (0 + crop, 0, nw, h)
        newim = im.crop(box)
        if left:
            n = pno
        else:
            n = pno + 1
        name = '%sp%03d.png' % (path, n)
        print '  ->', name, box
        newim.save(name)

        # 取右边页
        box = (nw, 0, w - crop, h)
        newim = im.crop(box)
        if left:
            n = pno + 1
        else:
            n = pno
        name = '%sp%03d.png' % (path, n)
        print '  ->', name, box
        newim.save(name)
        
        return True



# 获取图片文件列表，自动分页
#
# 参数
#   match: 通配符
#   pno: 起始编号
#   left: True 从左到右计数
#   crop: 固定切边宽度
#   cropstart: 从第几个文件开始切边，从1计数
def split_pics(match, pno=1, left=False, crop=0, cropstart=1):
    lst = glob.glob(match)
    lst.sort()
    n = pno
    i = 1
    for l in lst:
        if i < cropstart:
            toCrop = 0
        else:
            toCrop = crop
        s = split_page(l, n, left, toCrop)
        if s:
            n += 2
        else:
            n += 1
        i += 1



# 将指定的图片进行自动切边操作
#
# 参数
#   match: 通配符
def crop_pics(match, portrait=False):
    lst = glob.glob(match)
    lst.sort()
    for l in lst:
        page_auto_crop(l, portrait)
        

# ============================================================
# 图片处理工具

# 识别相似颜色
def is_color_similar(c1, c2):
    dv = 30
    d1 = abs(c1[0] - c2[0]) # red
    d2 = abs(c1[1] - c2[1]) # green
    d3 = abs(c1[2] - c2[2]) # blue
    if d1 <= dv and d2 <= dv and d3 <= dv:
        return True
    else:
        return False
    
    
# 将垂直拼接的多张图片进行拆分
def split_pic_v(fname, similar_ratio = 0.6, min_height = 300, line1 = 0, line2 = 0):
    debug = line1 != 0 or line2 != 0
##    os.chdir('g:/')
    
    im = Image.open(fname)
    if im.mode != 'RGB':
        im = im.convert('RGB')
        
    w = im.size[0]
    h = im.size[1]
    similar = w * similar_ratio
    min_h = min_height
    print 'PROCESSING %s %d x %d mode:%s' % (fname, w, h, im.mode)
    print 'SIMILAR:%d MIN-HEIGHT:%d' % (similar, min_h)

    name_t = '%s-%02d.png'
    name = fname[ : string.rfind(fname, '.')]
    
    bits = im.getdata()
    result = []
    
    for ln in range(1, h):
        ll = (ln - 1) * w # 上一行
        cl = ln * w # 当前行
        s = 0
        for i in range(w):
            if is_color_similar(bits[cl + i], bits[ll + i]):
                s += 1

        result.append(s)

    no = 1
    ly = 0
    if debug:
        print '========== DEBUG INFO =========='
        for i in range(line1 + 1, line2):
            mark = ''
            if result[i] < similar and i - ly > min_h:
                mark = '<-- %d' % similar
                ly = i
            print '  %5d: %5d %s' % (i, result[i], mark)
    else:
        count = len(result)
        for i in range(1, count):
            if result[i] < similar and i - ly > min_h:
                print '  edge at %d' % (i)
                
                nn = name_t % (name, no)
                box = (0, ly, w, i)
                print '  ->', nn, box
                newim = im.crop(box)
                newim.save(nn)
                
                ly = i
                no += 1
                    
        if h - ly > min_h:
            print '  last'
            nn = name_t % (name, no)
            box = (0, ly, w, h)
            print '  ->', nn, box
            newim = im.crop(box)
            newim.save(nn)


# ============================================================

# 保存文本行
def save_line(ostream, text):
    line = u'　　'.encode('gbk') + text.encode('gbk') + '\n'
    ostream.write(line)


# 分解文件名
def parse_fname(name):
    n = unicode(name, 'gbk')
    p1 = string.find(n, ')')
    p2 = string.rfind(n, '.')
    return n[p1+1:p2]

    
# 对文本进行分析，格式化。主要针对古典小说
def parse_text(fname, oname):
    f = open(fname, 'r')
    all_lines = f.readlines()
    f.close()

    c = 1
    hzblank = u'　'
    enblank = ' '
    biaodian = u'，。'

    rt = []

    # 分析文件名，获取标题
    title = parse_fname(fname)
    title = string.split(title, hzblank)
##    print title

    # 去除空格
    for line in all_lines:
        line = unicode(line, 'gbk')

        buf = ''
        blank_c = 0
        length = len(line)
        for i in range(length):
            if line[i] == enblank or line[i] == hzblank:
                blank_c += 1
                continue

            # 当连续空格数多于2个时，换行
            if blank_c >= 2 and len(buf):
                rt.append(buf)
                buf = ''
            
            blank_c = 0
            if line[i] != '\n':
                buf += line[i]

        if len(buf):
            rt.append(buf)

    # 保存结果
    out = open(oname,'w')

    # 保存标题
    for line in title:
        line = line.encode('gbk') + '\n'
        out.write(line)
    out.write('\n')
    
    for buf in rt:

        length = len(buf)
        # 检测是否是诗句
        if length % 8 == 0:
            n = length / 8
            err = False
            for i in range(1, n+1):
                if string.find(biaodian, buf[i * 8 - 1]) == -1:
                    err = True
                    break
            if not err:
                for i in range(n):
                    save_line(out, buf[i*8 : (i+1)*8])
                buf = ''

        if len(buf):
            save_line(out, buf)
        
    out.close()


# 对一组txt操作
def format_all():
    lst = glob.glob('/(*.txt')
    lst.sort()

    c = 1
    for l in lst:
        name = 'ch%03d.txt' % c
        c += 1
        parse_text(l, name)
        print l, '==>', name



# 对 html 中的图片重新编号
class pageParser(SGMLParser):
    def reset(self):
        self.links = []
        SGMLParser.reset(self)

    def start_img(self, attrs):
        for k,v in attrs:
            if k == 'src':
                self.links.append(v)

def renamePicInPage(fname, order=1):

    parser = pageParser()
    f = open(fname, 'r')
    parser.reset()
    parser.feed(f.read())
    parser.close()
    f.close()

    n = order
    for l in parser.links:

        if string.find(l, 'http://') >= 0:
            l = l[string.rfind(l, '/') : ]
            name = fname[ : string.rfind(fname, '.')]
            l = name + '_files' + l
            
        if os.access(l, os.F_OK):
            pos = string.rfind(l, '/')
            path = l[:pos]
            pos = string.rfind(l, '.')
            ext = l[pos:]
            name = '%s/p%03d%s' % (path, n, ext)
            n += 1
            print l, '-->', name
            os.rename(l, name)
    

# 创建图片下载列表html
def gen_pic_htm(fname, url):
    f = open(fname, 'r')
    data = f.read()
    f.close()
    lst = string.split(data, '|');
    lst.sort()

    f = open('out.htm', 'w')
    f.write('<html><body>\n')
    i = 1
    for name in lst:
        n = string.rfind(name, '/') + 1
        txt = '<a href="%s%s">%03d_%s</a><br/>\n' % (url, name, i, name[n:])
        f.write(txt)
        i += 1
    f.write('</body></html>\n')
    f.close()


# 下载文件
def download(host, url, referer, fname):
    headers = {'Accept':'image/png,image/*;q=0.8,*/*;q=0.5',
               'Accept-Encoding':'gzip, deflate',
               'Accept-Language':'zh-cn,zh;q=0.8,en-us;q=0.5,en;q=0.3',
               'Connection':'keep-alive',
               'Referer':referer,
               'User-Agent':'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_1) AppleWebKit/600.1.25 (KHTML, like Gecko) Version/8.0 Safari/600.1.25'}

    conn = httplib.HTTPConnection(host)
    conn.request('GET', url, '', headers)
    response = conn.getresponse()
    if response.status >= 300 and response.status < 400:
        for h in response.getheaders():
            if h[0] == 'location':
                print h[0], h[1]
    if response.status == 200:
        data = response.read();
        f = open(fname, 'wb')
        f.write(data)
        f.close()
        print url, '->', fname, len(data)
    else:
        print url, '->', fname, 'error', response.status


# http://www.imanhua.com 图片下载
# url 图片地址
# num 图片总量
# referer http 引用地址
def get_imanhua(url, num, referer):
    uri = urlparse.urlparse(url)
    path = urllib.unquote(uri.path)
    pos = string.rfind(path, '_')
    if pos == -1:
        pos = string.rfind(path, ' ')
    ext = string.rfind(path, '.')
    for i in range(num):
        link = '%s%03d%s' % (path[:pos+1], i+1, path[ext:])
        name = link[string.rfind(link, '/') + 1 :]
        name = string.replace(name, ' ', '_')
        link = uri.scheme + '://' + uri.netloc + urllib.quote(link)
        # print link, name
        if os.access(name, os.W_OK):
            continue
        download(uri.netloc, link, referer, name)


# 从 http://hhcomic.com 下载漫画图片
# 该漫画网图片下载地址列表已加密，需要进入 Debugger 获取解码后的地址列表，
# 存入 t 文件，本脚本会读取该文件依次下载图片
# host:     图片下载站，如: '33.3348.net:9393'
# referer:  引用地址，如: 'http://paga.hhcomic.net/1824164/hh171960.htm?s=11'
# dm:       编号，如: '11'
# prefix:   图片文件名前缀，默认 'mh'
def down_pic(host, referer, dm, prefix='mh'):

    if host == '':
        print 'need host!'
        return
    if referer == '':
        print 'need referer!'
        return
    if dm == '':
        print 'need dm!'
        return

    use_proxy = False
    PROXY = '172.19.1.2:9217'
    
    f = open('t', 'r')
    data = f.read()
    f.close()

    data = string.strip(data)
    data = string.replace(data, '"', '')

    lst = string.split(data, '|')

    i = 1
    for name in lst:
        if use_proxy:
            url = 'http://%s/dm%s/%s' % (host, dm, name)
        else:
            url = '/dm%s/%s' % (dm, name)
        fn = '%s_%03d.jpg' % (prefix, i)
        info = '(%03d/%03d)' % (i, len(lst))
        i += 1

        if os.access(fn, os.W_OK):
            continue
        
        headers = {'Accept':'image/png,image/*;q=0.8,*/*;q=0.5',
                   'Accept-Encoding':'gzip, deflate',
                   'Accept-Language':'zh-cn,zh;q=0.8,en-us;q=0.5,en;q=0.3',
                   'Connection':'keep-alive',
                   'Referer':referer,
                   'User-Agent':'Mozilla/5.0 (Macintosh; Intel Mac OS X 10.9; rv:30.0) Gecko/20100101 Firefox/30.0'}

        if use_proxy:
            use_host = PROXY
        else:
            use_host = host
        conn = httplib.HTTPConnection(use_host)
        conn.request('GET', url, '', headers)
        response = conn.getresponse()
        if response.status == 200:
            data = response.read();
            f = open(fn, 'wb')
            f.write(data)
            f.close()
            print info, url, '->', fn, len(data)
        else:
            print info, url, '->', fn, 'error'
    

def test_decode(fname):
    f = open(fname, 'r')
    data = f.read()
    f.close()

    print base64.b64decode(data)
    
    
# ============================================================
# PDF图片处理程序
#
if __name__ == "__main__":
    
    print "PDF maker (C) 2010 migrsoft"
    wp = 'd:/z'
    print 'enter work path', wp
    os.chdir(wp)
    if not os.access('temp', os.W_OK):
        print 'creating work directory...'
        os.mkdir('temp')
    

