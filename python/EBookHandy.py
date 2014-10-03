# -*- coding: utf-8 -*-

# EBookHandy (C) 2002-2014 migrsoft
# Version: 1.1
# Email: migr@qq.com
# Created date: 2002.8


from sgmllib import SGMLParser
import os, glob, stat, time
import string
import re
import subprocess
import exifread

# 小标题不具备的标号
punct_not_in_subtitle = u'！。？…：'

# 实体引用表
entity_table = {
    'ldquo'     : u'“',
    'rdquo'     : u'”',
    'lsquo'     : u'‘',
    'rsquo'     : u'’',
    'mdash'     : u'—',
    'hellip'    : u'…',
    'middot'    : u'·',
    'times'     : u'×',
    'nbsp'      : u' ',
    'amp'       : u'&',
    'alpha'     : u'α',
    'beta'      : u'β',
    'pi'        : u'π',
    'uuml'      : u'ü',
    'aacute'    : u'á',
    'divide'    : u'÷',
    'egrave'    : u'è',
    'eacute'    : u'é',
    'ccedil'    : u'?',
    'deg'       : u'°',
    'agrave'    : u'à',
    'lt'        : u'<',
    'gt'        : u'>',
    'permil'    : u'‰',
    'oacute'    : u'ó',
    'quot'      : '"',
    'ugrave'    : u'ù',
    'igrave'    : u'ì',
    'larr'      : u'←',
    'rarr'      : u'→',
    '-'         : u'-'
    }

############################################################
# Functions work on single file.
#

class Html2TextParser(SGMLParser):
    'Convert file in html format to pure text.'

    def reset(self):
        SGMLParser.reset(self)
        self.text = []
        self.start = 0
        self.ignore_style = 0

    def add_crlf(self):
        self.text.append(u'\n')
        
    def handle_charref(self, ref):
        if self.start:
            self.text.append(unichr(string.atoi(ref)))
        
    def handle_entityref(self, ref):
        if self.start:
            if ref in entity_table.keys():
                self.text.append(entity_table[ref])
            else:
                print ref

    def handle_data(self, text):
        if self.start and not self.ignore_style:
            self.text.append(text)
        
    def start_body(self, attrs):
        self.start = 1

    def end_body(self):
        self.start = 0

    def start_style(self, attrs):
        self.ignore_style = 1

    def end_style(self):
        self.ignore_style = 0
    
    # Table format handle.
    def start_th(self, attrs):
        self.add_crlf()
    
    def start_td(self, attrs):
        self.add_crlf()

    def do_br(self, attrs):
        if self.start:
            self.add_crlf()
        
    def start_p(self, attrs):
        self.add_crlf()

    def start_pre(self, attrs):
        self.add_crlf()

    def start_div(self, attrs):
        if self.start:
            self.add_crlf()

    def end_div(self):
        if self.start:
            self.add_crlf()


# 苗疆道事
# http://miaojiangdaoshi.513gp.org/
class mjdsParser(Html2TextParser):

    def reset(self):
        Html2TextParser.reset(self)

    def start_body(self, attrs):
        self.start = 0

    def start_div(self, attrs):
        for k, v in attrs:
            if k == 'class' and v == 'chaptertitle clearfix':
                self.start = 1
            elif k == 'class' and v == 'bookcontent clearfix':
                self.start = 1
        Html2TextParser.start_div(self, attrs)

    def end_div(self):
        self.start = 0

    def handle_data(self, text):
        if self.start:
            #print '[', text, ']'
            if text == '><br':
                return
            if text[:7] == 'http://':
                return
            if text[0] == '>':
                text = text[1:]
            if string.find(text, 'ps:') != 0:
                Html2TextParser.handle_data(self, text)


# 扎纸匠
class zzjParser(Html2TextParser):

    def reset(self):
        Html2TextParser.reset(self)

    def start_body(self, attrs):
        self.start = 0

    def start_h1(self, attrs):
        self.start = 1

    def end_h1(self):
        self.start = 0

    def start_div(self, attrs):
        for k, v in attrs:
            if k == 'id' and v == 'BookText':
                self.start = 1

    def end_div(self):
        self.start = 0

    def handle_data(self, text):
        if self.start:
            text = string.lower(text)
            # print '[', text, ']'
            text = string.replace(text, u'趣~读~屋', '')
            text = string.replace(text, u'趣/读/屋', '')
            text = string.replace(text, u'趣*读/屋', '')
            text = string.replace(text, u'趣*讀/屋', '')
            text = string.replace(text, 'www.quduwu.com', '')
            text = string.replace(text, 'www.ziyouge.com', '')
            if string.find(text, u'手机用户') != -1:
                self.start = 0
                return
            pos = string.find(text, u'正文')
            if pos >= 0:
                text = text[pos + 2:]
                pos = string.find(text, u'为')
                if pos != -1:
                    text = text[:pos]
            Html2TextParser.handle_data(self, text)


# 搜狐读书
# http://lz.book.sohu.com
class sohuParser(Html2TextParser):

    def reset(self):
        Html2TextParser.reset(self)
        self.div_levels = 0

    def start_body(self, attrs):
        self.start = 0

    def start_h2(self, attrs):
        self.start = 1
        self.div_levels = 1

    def end_h2(self):
        self.start = 0
        self.div_levels = 0

    def start_div(self, attrs):
        for k, v in attrs:
            if k == 'class' and v == 'book_con':
                self.start = 1
        if self.start:
            self.div_levels += 1

    def end_div(self):
        if self.start:
            self.div_levels -= 1
            if self.div_levels == 0:
                self.start = 0

    def handle_data(self, text):
        if self.start and self.div_levels == 1:
            Html2TextParser.handle_data(self, text)


def ParserFactory(kind):

    if kind == 'mjds':
        return mjdsParser()
    elif kind == 'sohu':
        return sohuParser()
    elif kind == 'zzj':
        return zzjParser()
    else:
        return Html2TextParser()


def IncludeChar(text, match):
    str_len = len(text)
    for i in range(0, str_len):
        if string.find(match, text[i]) >= 0:
            return True
    return False


# 检测是否为章节标题
def isChapter(txt, toc):

    # 常见文字
    tm = [u'引子', u'楔子',
          u'自序', u'序言', u'序一', u'序二', u'推荐序', u'序',
          u'前言', u'引言',
          u'后记', u'译后记', u'尾声', u'附录',
          u'※']
    
    for m in tm:
        p1 = string.find(txt, m)
        if p1 == 0:
            return (1, len(m))

    p1 = string.find(txt, u'第')
    
    # 第几章、回形式
    p2 = string.find(txt, u'章')
    if p2 == -1:
        p2 = string.find(txt, u'回')
        
    if p1 == 0 and p2 > 0:
        if p2 - p1 <= 6:
            return (1, p2 + 1)
        
    # 第几节形式
    p2 = string.find(txt, u'节')
    if p1 == 0 and p2 > 0:
        if p2 - p1 < 5:
            return (2, p2 + 1)

    # 第几部形式
    p2 = string.find(txt, u'部')
    if p1 == 0 and p2 > 0:
        if p2 - p1 < 5:
            return (9, p2 + 1)
        
    # 第几卷形式
    p2 = string.find(txt, u'卷')
    if p1 == 0 and p2 > 0:
        if p2 - p1 < 5:
            return (8, p2 + 1)

    # 第几编形式
    p2 = string.find(txt, u'编')
    if p1 == 0 and p2 > 0:
        if p2 - p1 < 5:
            return (7, p2 + 1)
    
    # “序号、”形式
    chsn = u'一二三四五六七八九十〇'
    p2 = string.find(txt, u'、')
    if p2 < 0:
        p2 = string.find(txt, u'，')
    if p2 < 0:
        p2 = string.find(txt, u' ')
    if p2 < 0:
        p2 = string.find(txt, u'　')
    if p2 > 0 and p2 < 5:
        found = True
        for i in range(p2):
            if string.find(chsn, txt[i]) == -1:
                found = False
                break
        if found:
            return (2, p2 + 1)

    # 1. 形式
    num = u'1234567890'
    p2 = string.find(txt, u'.')
    if p2 == -1:
        p2 = string.find(txt, u'、')
    if p2 > 0 and p2 < 5:
        found = True
        for i in range(p2):
            if string.find(num, txt[i]) == -1:
                found = False
                break
        if found:
            return (3, p2 + 1)

    # 纯数字标题
    if re.match('^\d{1,3}$', txt):
        return (3, len(txt))

    # 从提供的目录文件中匹配标题
    if len(toc) > 0:
        if string.find(toc, txt) >= 0:
            return (5, len(toc))

    return (0, 0)


# 提取标题
def get_chapter(text):
    m1 = u'节'
    m2 = u'（'
    m3 = u'。'
    if string.find(text, m3) >= 0:
        return ''
    p1 = string.find(text, m1)
    p2 = string.find(text, m2)
    if p2 == -1:
        p2 = string.find(text, '(')
    if p1 < 0 and p2 < 0:
        return ''
    if p1 < 0:
        p1 = -1
    if p2 < 0:
        p2 = len(text)
    return string.strip(text[p1+1:p2])


MAX_TITLE_LENGTH = 36
    
# 删除文本段落中的前后空格
def StripText(filename, style_marker = False):
    'Strip blank line in text file.'

    check_chapter = True # 是否自动识别章节
    check_title = True # 是否对标题不加空格，且加入空行
    create_new = True # 是否创建新文件
    para_indent = True # 是否增加段落缩进

    coll_title = False # 收集小标题
    coll_tail = False # 收集段落结束字符
    # style_marker = False # 是否生成样式替换标记

    tit = []
    indent = u'　　'

    print filename

    # 读取目录文件，用于识别章节
    toc = ''
    try:
        txt = open('toc', 'r')
    except IOError:
        print 'no toc found'
    else:
        lines = txt.readlines()
        txt.close()
        for l in lines:
            l = string.strip(l)
            if len(l) == 0:
                continue
            l = unicode(l, 'gbk', 'ignore')
            toc += l
            toc += '\n'
    if toc != '':
        print '===== TOC ====='
        print toc
        print '==============='

    # 读取行尾结束字符
    tail = '' # 段落结束
    if not coll_tail:
        try:
            txt = open('tail.txt', 'r')
        except IOError:
            print 'no tail.txt found'
        else:
            lines = txt.readlines()
            txt.close()
            for l in lines:
                l = string.strip(l)
                if len(l) == 0:
                    continue
                l = unicode(l, 'gbk', 'ignore')
                tail += l
                tail += '\n'

    # 读取文本
    txt = open(filename, 'r')
    lines = txt.readlines()
    txt.close()

    if create_new:
        newname = string.replace(filename, '.txt', '_new.txt')
    else:
        newname = filename
    txt = open(newname, 'w')
    
    row = 0
    last = -2
    chap = '-none-' # 记录标题

    for line in lines:
        line = string.strip(line)

        if len(line) == 0:
            continue

        line = unicode(line, 'gbk', 'ignore')

        # 删除段落前导空格
        length = len(line)
        i = 0
        while i < length:
            if line[i] == u'　' or line[i] == u' ': # 全角或半角空格
                i += 1
            else:
                break
        line = line[i:]

        # 删除段落后续空格
        length = len(line)
        i = length - 1
        while i >= 0:
            if line[i] == u'　' or line[i] == u' ':
                i -= 1
            else:
                break
        if i < 0:
            continue
        else:
            line = line[:i+1]

        if len(line) == 0:
            continue

        if line[0] == '(':
            continue

        # 收集段落未尾字符
        if coll_tail:
            if string.find(tail, line[-1]) == -1:
                tail += line[-1]
                tail += '\n'
            
        # 识别章节
        ischapter = False
        if check_chapter and len(line) < MAX_TITLE_LENGTH:

            ret = isChapter(line, toc)
            found = ret[0]
            pos = ret[1]

            if not coll_tail and found == 0 and tail != '':
                if string.find(tail, line[-1]) == -1:
                    found = 2
                
##            ttt = get_chapter(line)
##            if len(ttt) > 0:
##                if ttt == chap:
##                    row += 1
##                    continue
##                else:
##                    print ttt
##                    chap = ttt
##                    line = u'※ ' + chap
##                    found = 0
##                    ischapter = True
##                    txt.write('\n')

            if found > 0:
                t1 = string.strip(line[:pos])
                t2 = string.strip(line[pos:])
                if len(t2) > 0:
                    line = t1 + ' ' + t2
                else:
                    line = t1
                
                mark = ''
                if last == row - 1:
                    mark = '*'
                print '%s %s' % (line, mark)
                
                if style_marker:
                    line = '##%d##%s' % (found, line)
                else:
                    txt.write('\n')
                
                ischapter = True
                last = row

        # 收集标题
        if coll_title and len(line) < 30:
            ch_punct = u'，。…—！？：；'
            if string.find(ch_punct, line[-1]) == -1:
                tit.append(line)

        if para_indent:
            # 除首段外,每段加入2个中文空格
            if (row > 0 or not check_title) and not ischapter:
                line = indent + line

        txt.write(line.encode('gbk', 'ignore') + '\n')
        if ischapter and not style_marker:
            txt.write('\n') # 在章节标题后加入一空行

##        if check_title and row == 0 and not style_marker: #小标题空一行
##            txt.write('\n')
            
        row += 1

    txt.close()

    if coll_tail:
        print 'save tail.txt'
        f = open('tail.txt', 'w')
        f.write(tail.encode('gbk', 'ignore'))
        f.close()

    if coll_title:
        f = open('toc_', 'w')
        for ln in tit:
            f.write(ln.encode('gbk','ignore') + '\n')
        f.close()


# 拆分文本
def SplitText(filename, number=1):

    txt = open(filename, 'r')
    lines = txt.readlines()
    txt.close

    xhtml_beg = """<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh-CN">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<link rel="stylesheet" type="text/css" href="../Styles/novel.css"/>
"""
    xhtml_mid = """</head>
<body>
"""
    xhtml_end = """</body>
</html>"""

    no = number
    buf = xhtml_beg
    add = False
    title = False
    
    for p in lines:
        p = unicode(p, 'gbk','ignore')
        p = string.strip(p)
        p_len = len(p)
        
        if p_len > 0:
            if p_len < MAX_TITLE_LENGTH:
                ret = isChapter(p, '')
                if ret[0]:
                    buf += xhtml_end
                    if add:
                        splitTextOutput(no, buf)
                        no += 1
                        
                    buf = xhtml_beg
                    add = False
                    title = True

##                    if no == 5:
##                        break

            p = p.encode('utf8')
            
            if not add:
                htm = '<title>%s</title>\n' % p
                buf += htm + xhtml_mid
                add = True

            if title:
                htm = '<h1>%s</h1>\n' % p
                title = False
            else:
                htm = '<p>%s</p>\n' % p
            buf += htm

    buf += xhtml_end
    if add:
        splitTextOutput(no, buf)
                
def splitTextOutput(no, content):
    name = 'chapter%04d.htm' % no
    print name
    f = open(name, 'w')
    f.write(content)
    f.close()



def AdjustComment(filename):
    ''

    file = open(filename, 'r')
    lines = file.readlines()
    file.close()

    file = open(filename, 'w')
    for line in lines:
        pos = string.find(line, '    　　')
        if pos >= 0:
            file.write(line[:pos] + '\n')
            file.write(string.strip(line[pos:]) + '\n')
        else:
            file.write(line)
    file.close()


def JoinBreakLine(filename, detail=False):
    'Join one or more hard break lines into one normal paragraph.'

    print u'段落拼接: ', filename

    punct_para_start = u'　■□●○★☆▲△§※'
    punct_para_end = u'。！？”…'
    
    f = open(filename, 'r')
    lines = f.readlines()
    f.close()

    avail_lines = 0
    word_total = 0
    text = []
    append_bl = False
    for l in lines:
        l = string.rstrip(l)
        l = unicode(l, 'gbk', 'ignore')
        if len(l):
            avail_lines += 1
            word_total += len(l)
            text.append(l)
            append_bl = False
        else:
            if not append_bl:
                text.append('\n')
                append_bl = True

    if avail_lines == 0:
        print u'内容不足!'
        return

    del lines
    
    line_width = word_total / avail_lines

    if detail:
        print u'行数: %d 可能的拆行宽度: %d ' % (avail_lines, line_width)

##    print '---------------------------------------'
##    for l in text:
##        print l
##    print '---------------------------------------'

    # 拼接段落

    filename = string.replace(filename, '.txt', '_new.txt')
    f = open(filename, 'w')

    new_line = 0
    for line in text:
        if string.find(punct_para_start, line[0]) >= 0:
            f.write('\n')

        if new_line:
##            if line[0] == u'　' or line[0] == ' ':
##                f.write('\n')
            f.write('\n')
            new_line = 0
	
        if len(line) > line_width:
##            print line[-1]
            if string.find(punct_para_end, line[-1]) >= 0:
                new_line = 1
        else:
            new_line = 1
            
        f.write(line.encode('gbk', 'ignore'))

    f.close()


def DeleteLine(filename, head=0, tail=0):
    'Delete all specify lines in front or tail of this file.'
    
    src = open(filename, 'r')
    lines = src.readlines()
    src.close()

    num = len(lines)
    if num < head or num < tail:
        return

    output_start = head
    output_end = num - tail

    dst = open(filename, 'w')
    i = 0
    for line in lines:
        if i >= output_start and i < output_end:
            dst.write(line)
        i += 1
    dst.close()


def DeleteSpecifyLine(filename, specify_lines):
    'Delete all lines include specify content.'

    file = open(filename, 'r')
    lines = file.readlines()
    file.close()

    num = len(specify_lines)
    if num == 0: return

    file = open(filename, 'w')
    for line in lines:
        i = 0
        found = 0
        # Ignore \n at tail of line when compare.
        l1 = unicode(line[:-1], 'gbk', 'ignore')
        while i < num:
            l2 = unicode(specify_lines[i], 'gbk', 'ignore')
            if l1 == l2:
                found = 1
                break
            elif string.find(l1, l2) == 0:
                found = 1
                break
            i += 1
            
        if found:
            continue
        else:
            file.write(line)

    file.close()


def utf2gbk(filename):
    '转换 utf8 到 gbk'

    f = open(filename, 'r')
    text = f.read()
    f.close()

    try:
        utext = unicode(text, 'utf8', 'ignore')
    except UnicodeDecodeError:
        print '%s is not utf8!'
    else:
        f = open(filename,'w')
        f.write(utext.encode('gbk', 'ignore'))
        f.close()


def formatPara(fname, out='out.txt'):
    f = open(fname, 'r')
    lines = f.readlines()
    f.close()

    p1 = string.find(fname, ')')
    p2 = string.rfind(fname, '.')
    title = fname[p1+1 : p2]

    f = open(out, 'w')
    title = unicode(title, 'gbk', 'ignore')
    f.write(title.encode('gbk','ignore') + '\n')
    for ln in lines:
        ln = string.strip(ln)
        if len(ln) == 0:
            continue
        ln = unicode(ln, 'gbk', 'ignore')
        m = string.split(ln, u'　　')
        for c in m:
            f.write(c.encode('gbk', 'ignore') + '\n')
    f.close()

def multiFormatPara(path):
    os.chdir(path)
    lst = glob.glob('*.txt')
    lst.sort()
    i = 1
    for fn in lst:
        print fn
        name = 'ch%03d.txt' % i
        i += 1
        formatPara(fn, name)
        

############################################################
# Functions work on one or more files.
#

def MultiHtml2Text(kind='-'):
    'Convert all files in html format to pure text.'

    all_kinds = 'mjds, sohu, zzj, normal'
    if string.find(all_kinds, kind) == -1:
        print 'kind:', all_kinds
        return

    file_list = glob.glob('*.htm')
    if len(file_list) == 0:
        file_list = glob.glob('*.html')
    if len(file_list) == 0:
        file_list = glob.glob('*.xhtml')
        
    file_list = sort_filename(file_list)
    total = len(file_list)
    i = 0
    for fn in file_list:
        if kind == 'mjds':
            DeleteLine(fn, 17, 0)

        i += 1
        txtfile = fn[:string.rfind(fn, '.')] + '.txt'
        print '(%3d of %3d) Converting %s ==> %s' % (i, total, fn, txtfile)

        ConvHtml2Text(fn, txtfile, kind)

    print 'Total: %d' % total


def h2t(kind='-'):
    MultiHtml2Text(kind)


def MultiTextStrip():
    print 'Strip all text files.'

    file_list = []
    for file in os.listdir('.'):
        if string.lower(file[-4:]) == '.txt':
            file_list.append(file)

    file_list.sort()
    total = len(file_list)
    for file in file_list:
        StripText(file)

    print 'Total: %d' % total


def MultiTextAdjustComment():
    ''

    file_list = []    
    for file in os.listdir('.'):
        file = string.lower(file)
        if string.rfind(file, '.txt') >= 0:
            file_list.append(file)

    file_list.sort()
    total = len(file_list)
    i = 0
    for file in file_list:
        i += 1
        #print '(%d of %d) Adjusting %s...' % (i, total, file)
        AdjustComment(file)

    print 'Total: %d' % total


def MultiTextJoinBreakLine():
    'Do join break line at all text files.'

    file_list = glob.glob('*.txt')
    file_list.sort()
    total = len(file_list)
    i = 0
    for fn in file_list:
        i += 1
        #print '(%d of %d) Parsering %s...' % (i, total, fn)
        JoinBreakLine(fn)

    print 'Total: %d' % total


def MultiTextDelLine(head=0, tail=0, ext='txt'):
    'Delete specify line in all text files.'

    lst = glob.glob('*.%s' % ext)
    lst = sort_filename(lst)

    total = len(lst)
    for n in lst:
        DeleteLine(n, head, tail)

    print 'Total: %d' % total


def MultiTextDelSpecifyLine(specify_lines):
    'Delete line include specify content in all text files.'

    file_list = []
    for file in os.listdir('.'):
        file = string.lower(file)
        if string.rfind(file, '.txt') >= 0:
            file_list.append(file)

    file_list.sort()
    total = len(file_list)
    i = 0
    for file in file_list:
        i += 1
        #print '(%d of %d) Processing %s...' % (i, total, file)
        DeleteSpecifyLine(file, specify_lines)

    print 'Total: %d' % total

def MultiTextFormTitle():
    for name in glob.glob('*.txt'):
        file = open(name, 'r')
        lines = file.readlines()
        file.close()

        file = open(name, 'w')
        i = 1
        for line in lines:
            if i == 1:
                line = unicode(line, 'gbk', 'ignore')
                if line[:2] == u'　　':
                    line = line[2:]
                file.write(line.encode('gbk') + '\n')
                i += 1
            else:
                file.write(line)
        file.close()

def MultiUtf8ToGbk():

    lst = glob.glob('*.txt')
    for l in lst:
        utf2gbk(l)
        

        
############################################################
# Some useful combinations.
#

def MultiTextFormat(do_join=0, line_width=70):
    'Format all text files.'

    MultiTextStrip()
    if do_join:
        MultiTextJoinBreakLine(line_width)


def DoAllWork(do_join=0, line_width=70):
    'Do all work in normal case just one step.'

    MultiHtml2Text()
    MultiTextFormat(do_join, line_width)


############################################################
# Other useful common utilities.
#

def ConvertText2Html():
    'Convert formatted text file to indexical html.'

    file_list = []
    for file in os.listdir('.'):
        if string.rfind(file, '.txt') >= 0:
            file_list.append(file)

    file_list.sort()
    total = len(file_list)
    i = 0
    title_list = []
    for file in file_list:
        i += 1
        htmfile = '%s.htm' % file[:string.rfind(file, '.txt')]
        print '(%d of %d) Converting %s ==> %s' % (i, total, file, htmfile)

        f = open(file, 'r')
        lines = f.readlines()
        f.close()

        f = open(htmfile, 'w')
        f.write('<html><body>\n')

        get_title = 1
        for line in lines:
            line = string.strip(line)

            if get_title:
                title_list.append('<li><a href="%s">%s</a>\n' % (htmfile, line))
                get_title = 0

            f.write('%s<br>\n' % line)

        f.write('\n</body></html>')
        f.close()

    print 'Output index.htm...'
    f = open('index.htm', 'w')
    f.write('<html><body>\n<ul>\n')

    for line in title_list:
        f.write(line)

    f.write('\n</ul>\n</body></html>')
    f.close()

    print 'All finished!'


# 将多个文本合并
# 参数：
#   lineSpace: 在每个文件间加入空行
#   skipTitle: 忽略文件中的第一行（标题行）
#   addTag: 加标题标签
def MultiText2One(lineSpace=2, skipTitle=False, addTag=True):
    'Join all text files to one.'

    file_list = glob.glob('*.txt')
    file_list = sort_filename(file_list)
    total = len(file_list)
    
    i = 0
    onefile = open('all', 'w')
    for fn in file_list:
        i += 1
        print '(%d of %d) Adding %s...' % (i, total, fn)
        
        f = open(fn, 'r')
        lines = f.readlines()
        f.close()
        
        if skipTitle:
            lines = lines[1:]
        text = ''.join(lines)
        if not skipTitle and addTag:
            onefile.write('##2##')
        onefile.write(text)

        j = 0
        while j < lineSpace:
            onefile.write('\n')
            j += 1

    onefile.close()

    print 'All finished!'


def t2one():
    MultiText2One(2, False, False)
    os.rename('all', 'book.txt')


def HZFull2Half():
    ''
##    return

    HZIndex = [u'１', u'２', u'３', u'４', u'５', u'６', u'７', u'８', u'９', u'０']
    HZTable = {u'１':'1', u'２':'2', u'３':'3', u'４':'4', u'５':'5', \
               u'６':'6', u'７':'7', u'８':'8', u'９':'9', u'０':'0'}

    file_list = glob.glob('*.txt')
    file_list.sort()
    total = len(file_list)
    HZIndex_len = len(HZIndex)

    i = 0
    for name in file_list:
        i += 1
        print '(%d of %d) Replacing %s...' % (i, total, name)
        f = open(name, 'r')
        lines = f.readlines()
        f.close()

        f = open(name, 'w')
        for line in lines:
            line = unicode(line, 'gbk', 'ignore')
            line = string.replace(line, ' ', '')
            
            j = 0
            while j < HZIndex_len:
                pos = string.find(line, HZIndex[j])
                if pos != -1:
                    line = string.replace(line, line[pos], HZTable[HZIndex[j]])
                j += 1

            f.write(line.encode('gbk'))

        f.close()


def DeleteFiles(extName):
    print 'Deleteing files...'
    
    file_list = []
    for file in os.listdir('.'):
        if string.rfind(file, extName) >= 0:
            file_list.append(file)

    for file in file_list:
        os.remove(file)

    print 'OK!'

##################################################

def ConvHtml2Text(fin, fout, kind):

    parser = ParserFactory(kind)
    
    f = open(fin, 'r')
    txt = f.read()
    f.close()

    encode = 'utf8'
    if kind == 'zzj':
        encode = 'gbk'

    txt = unicode(txt, encode, 'ignore')
    
    parser.reset()
    parser.feed(txt)
    parser.close()

    f = open(fout, 'w')
    for  line in parser.text:
        f.write(line.encode('gbk', 'ignore'))
    f.close()


def easy():
    MultiHtml2Text()
    MultiTextJoinBreakLine()
    MultiTextStrip()



def PadTitle(title, spacing):
    new_title = ''
    for i in range(spacing):
        new_title += u'　'
    new_title += title
    return new_title

def MakeNovel(filename):
    print '>>> ' + filename
    
    doc = open(filename, 'r')
    lines = doc.readlines()
    doc.close()
    format_title = True
    l_no = 1
    l_min = 100
    l_max = 0

    contents = []
    for line in lines:
        line = string.strip(line)
        if len(line) == 0:
            continue

        line = unicode(line, 'gbk', 'ignore')

        if format_title:
            title = string.split(line, u'　')
            if len(title) == 3:
                contents.append(u'%s　%s' % (title[0], title[1]))
                contents.append(PadTitle(title[2], len(title[0])+1))
                l_no += 2
            else:
                contents.append(line)
                l_no += 1
            format_title = False
            contents.append('')
            l_no += 1
            continue

        if string.find(line, u'书香门第') >= 0:
            break

        line = string.replace(line, u'－－', u'——')
        line = string.replace(line, u'．．．', u'……')
        line = string.replace(line, u'∶', u'：')

        l_len= len(line)

        wrong = []
        for i in range(l_len):
            if string.find(string.ascii_letters, line[i]) >= 0:
                wrong.append(line[i])

        if len(wrong):
            for c in wrong:
                line = string.replace(line, c, u'□')
            print '>>>>> %d' % l_no

        contents.append(line)
        l_min = min(l_len, l_min)
        l_max = max(l_len, l_max)
        l_no += 1

    print '>>> total:%d min:%d max:%d\n' % (l_no, l_min, l_max)


    doc = open(filename, 'w')
    for line in contents:
        doc.write(line.encode('gbk') + '\n')
    doc.close()
    

def MultiMakeNovel():
    all_text = glob.glob('*.txt')
    for filename in all_text:
        MakeNovel(filename)
    print 'total: %d' % len(all_text)


def PunctConv(fname):
    punct_src = [u'『', u'』', u'「', u'」', u'後', u'麽', u'於']
    punct_dst = [u'“', u'”', u'“', u'”', u'后', u'么', u'于']
    
    print '>>> ' + fname

    doc = open(fname, 'r')
    lines = doc.readlines()
    doc.close

    contents = []
    for line in lines:
        line = unicode(line, 'gbk', 'ignore')

        i = 0
        for punct in punct_src:
            line = string.replace(line, punct, punct_dst[i])
            i += 1

        contents.append(line)

    doc = open(fname, 'w')
    for line in contents:
        doc.write(line.encode('gbk'))
    doc.close()


def PunctConvAll():
    all_text = glob.glob('*.txt')
    for fname in all_text:
        PunctConv(fname)
    print 'total: %d' % len(all_text)


def strip_list(lst):
    new_list = []
    for e in lst:
        e = string.strip(e)
        if len(e):
            new_list.append(e)
    return new_list

def make_e_txt(fname):
    doc_in = open(fname, 'r')
    all_lines = doc_in.readlines()
    doc_in.close()

    new_lines = []
    for line in all_lines:
        line = string.strip(line)
        if len(line) == 0:
            continue
        else:
            words = strip_list(string.split(line, ' '))
            line = ' '.join(words)
            line = string.replace(line, '\xa1\xaa', '--')
            new_lines.append(line + '\n\n')

    doc_out = open(fname, 'w')
    doc_out.write(''.join(new_lines))
    doc_out.close()

    print 'Ok!'
    

# 将文章按章节进行拆分
def break_text(name):

    count = 1

    src = file(name, 'r')
    contents = src.readlines()
    src.close()

    namet = 'g:/ch%02d.txt'
    filename =  namet % count
    dst = file(filename, 'w')
    print filename

    hzno = {1:u'一',
            2:u'二',
            3:u'三',
            4:u'四',
            5:u'五',
            6:u'六',
            7:u'七',
            8:u'八',
            9:u'九',
            10:u'十',
            11:''}

    chtp = u'第%s章'
    chmatch = chtp % hzno[count]

    chapter = False
    ignore = True
    no = '%d.' % count
    conti_blank_lines = 0
    for line in contents:
        
        line = unicode(line, 'gbk', 'ignore')
        line = string.strip(line)

        if len(line) == 0 and conti_blank_lines < 5:
            conti_blank_lines += 1
            continue

##        if conti_blank_lines == 5:
##            chapter = True
##        elif len(line) < 6 and line[0] == u'第' and line[-1] == u'章':
##            chapter = True
##            ignore = False
##        elif len(line) < 30 and string.find(line, no) == 0:
##            chapter = True
##            ignore= False
##        if len(line) < 50 and string.find(line, u'本书由派派小说论坛') >= 0:
##            chapter = True
##            ignore = True

        if len(line) < 20 and string.find(line, chmatch) >= 0:
            print '  chapter', chmatch
            chapter = True
            ignore = False

        if chapter:
            dst.close()
            count += 1
            chmatch = chtp % hzno[count]

##            if count == 15: # 循环控制
##                break
            
            filename = namet % count
            dst = file(filename, 'w')
            no = '%d.' % count
            print filename
            if not ignore:
                dst.write(line.encode('gbk') + '\n')

            chapter = False
            conti_blank_lines = 0
        else:
            dst.write(line.encode('gbk') + '\n')
            
    dst.close()
    


def to_gbf(filename):
    f = open(filename, 'r')
    lines = f.readlines()
    f.close()

    f = open(filename + '.new', 'w')

    for l in lines:
        l = string.strip(l)
        f.write('<txt>%s</txt>\n' % l)
    f.close()
    
        
    


# 转换所有utf8编码文件到gbk

def allutf2gbk(filename):
    file = open(filename, 'r')
    text = file.read()
    file.close()

    utext = unicode(text, 'utf8', 'ignore')

    file= open(filename,'w')
    file.write(utext.encode('gbk'))
    file.close()


# 将目录中指定类型的文件，以“[出版社]_书名.扩展名”的形式重命名。
# 单词之间使用“_”连接。
def rename_ebook_file(t, do = False):

    if t != 'chm' and t != 'pdf':
        print 'type must be pdf or chm!'
        return
    
    t = '*.' + t
    
    lst = glob.glob(t)
    lst.sort()
    for l in lst:
        if l[0] == '[':
            continue

        old = l
        publisher = ''
        name=''
        ext = ''
        ss = []

        # 查找出版社信息        
        pos = string.find(l, '-')
        if pos > 0:
            publisher = l[:pos]
            publisher = string.strip(publisher)
            l = l[pos+1:]
            l = string.strip(l)
            
        # 排除扩展名
        ext = string.lower(l[-4:])
        l = l[:-4]

        # 检测分隔符，空格、句点
        if string.find(l, ' ') > 0:
            ss = string.split(l, ' ')
        elif string.find(l, '.') > 0:
            ss = string.split(l, '.')
        elif string.find(l, '-') > 0:
            ss = string.split(l, '-')
        else:
            ss.append(l)

        # 拼接新文件名
        if len(publisher):
            name = '[%s]_' % publisher
        else:
            name = '[Unknown]_'
        name += '_'.join(ss)
        name += ext

        print old, '=>', name
        if do:
            os.rename(old, name)
##        break


def ren_ebook(t, do=False):
    if t != 'chm' and t != 'pdf':
        print 'rename chm or pdf only!'
        return

    match = '*.%s' % t
    lst = glob.glob(match)
    lst.sort()
    for l in lst:
        if l[0] != '[':
            continue

        pub = ''
        year = ''
        title = ''
        ext = ''

        p1 = string.find(l, '_')
        pre = l[:p1]

        m = string.split(pre, '[')
        c = 0
        for n in m:
            if c >= 1:
                p = n[:len(n)-1]
                if p[0] >= '0':
                    year = p
                else:
                    pub = p
            c += 1
        
        p2 = string.rfind(l, '.')
        ext = l[p2+1:]

        title = l[p1+1:p2]

        if pub != '' and year != '':
            name = '%s-%s_%s.%s' % (title, pub, year, ext)
        elif pub != '':
            name = '%s-%s.%s' % (title, pub, ext)
        else:
            name = '%s-%s.%s' % (title, year, ext)
        print name

        if do:
            os.rename(l, name)
        
        

# 针对双页漫画文件，对页码重新编号
def rename_comic_file():

    os.chdir(u'f:/Comics2/城市猎人 #3/')

    lst = glob.glob('*.*')
    lst.sort()
    if len(lst) % 2:
        print u'文件数不为偶数,是否有误?'
##        return

    for l in lst:
        l = string.strip(l)
        pos = string.rfind(l, '.')
        ext = l[pos:]
        n = l[1:pos]
        no = string.atoi(n)
        name = ''

        if no < 4:
            continue

        # 重新编号，奇数页+1；偶数页-1
        if no % 2:
            no = no - 1
        else:
            no = no + 1
        name = '%03d%s' % (no, ext)
        print l, '-->', name
        os.rename(l, name)
        

def ttt():
    s='author wuyulun@tom.com'
    l = len(s)
    m = ''
    for i in range(l):
        if i > 0:
            m += ','
        m += hex(ord(s[i]))
    print m



# 对索引页内的子页编号
class pageParser2(SGMLParser):
    
    def reset(self):
        self.links = []
        SGMLParser.reset(self)

    def start_a(self, attrs):
        for k,v in attrs:
            if k == 'href':
                self.links.append(v)

# 铁磨方式
def renameHtmInPage(fname, order=1):
    
    parser = pageParser2()
    f = open(fname, 'r')
    parser.reset()
    parser.feed(f.read())
    parser.close()
    f.close()

    for l in parser.links:
        l = string.strip(l)
        p = string.rfind(l, '/')
        if p > 0 and string.rfind(l, '_') > 0:
            name = l[p+1:]
            name_e = name + '.htm'
            if os.access(name_e, os.F_OK):
                name_n = '%03d.htm' % order
                order += 1
                print name_e, '->', name_n
                os.rename(name_e, name_n)


def renamePic(prefix, start=1):
    lst = glob.glob('*');
    lst.sort()

    for l in lst:
        if os.path.isdir(l):
            continue
        name = '%s%03d.jpg' % (prefix, start)
        start += 1
        print l, '-->', name
        os.rename(l, name)


def replace_text(fname, oldstr, newstr):
    f = open(fname, 'r')
    lines = f.readlines()
    f.close()

    f = open(fname, 'w')
    for line in lines:
        line = string.replace(line, oldstr, newstr)
        f.write(line)
    f.close()

def multi_replace_text(oldstr, newstr):
    lst = glob.glob('*.html')
    for l in lst:
        replace_text(l, oldstr, newstr)


INVALID_DATE_TIME = '00000000_000000'
DATE_TIME_PATTERN = '^\d\d\d\d-\d\d-\d\d \d\d\d\d\d\d$'

# 使用外部 exif 程序获取图片日期
def get_datetime_exif(fname):
    args = ['exif', '-t', '0x9003']
    args.append(fname)
    child = subprocess.Popen(args, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    stdout, stderr = child.communicate()
    if child.returncode:
        return INVALID_DATE_TIME

    marker = 'Value:'
    pos = string.find(stdout, marker)
    if pos == -1:
        return INVALID_DATE_TIME
    dt = string.strip(stdout[pos + len(marker):])
    dt = string.replace(dt, ':', '')
    dt = string.replace(dt, ' ', '_')
    return dt

# 使用 python exifread 库获取图片日期
def get_datetime_exifread(fname):
    f = open(fname, 'rb')
    tags = exifread.process_file(f)
    f.close()
    if len(tags) == 0:
        return INVALID_DATE_TIME
    marker = 'Image DateTime'
    if not tags.has_key(marker):
        return INVALID_DATE_TIME
    dt = tags[marker].values
    dt = string.replace(dt, ':', '')
    dt = string.replace(dt, '-', '')
    dt = string.replace(dt, ' ', '_')
    return dt    

# 使用文件编辑时间作为图片日期
def get_datetime_file(fname):
    dt = time.localtime(os.stat(fname)[stat.ST_MTIME])
    dt = time.strftime('%Y%m%d_%H%M%S', dt)
    return dt

# 使用文件名作为图片日期
def get_datetime_name(fname):
    pos = string.rfind(fname, '.')
    if pos == -1:
        return INVALID_DATE_TIME
    name = fname[:pos]
    if not re.match(DATE_TIME_PATTERN, name):
        return INVALID_DATE_TIME
    name = string.replace(name, '-', '')
    name = string.replace(name, ' ', '_')
    return name

# 换转 Nikon 相机文件名
def nikon_rename(exif=False, test=True, ext='jpg'):
    ext = string.lower(ext)
    lst = glob.glob('*.*')
    ext_len = len(ext)
    new_lst = []
    for n in lst:
        nl = string.lower(n)
        if string.find('dscn,cimg,img_,imag', nl[:4]) == -1:
            continue
        if nl[-ext_len:] != ext:
            continue
        new_lst.append(n)

    lst = new_lst
    lst.sort()

    last_name = ''
    last_count = 0
    count = 0
    for l in lst:
        if string.find('mp4,mov', ext) != -1:
            jpg_name = 'VID_'
        else:
            jpg_name = 'IMG_'

        info = ''
        if exif:
            #jpg_date_str = get_datetime_exif(l)
            jpg_date_str = get_datetime_exifread(l)
            if jpg_date_str == INVALID_DATE_TIME:
                info = '*'
                jpg_date_str = get_datetime_file(l)
                # jpg_date_str = get_datetime_name(l)
        else:
            jpg_date_str = get_datetime_file(l)

        jpg_name += jpg_date_str

        if jpg_name == last_name:
            last_count += 1
            jpg_name += '_%02d' % last_count
        else:
            last_name = jpg_name
            last_count = 0
        jpg_name += '.%s' % ext

        if l == jpg_name:
            print l, '--'
        else:
            print l, '->', jpg_name, info
            if not test:
                os.rename(l, jpg_name)
            count += 1

    print 'total:', len(lst), 'rename:', count


# 对文件名进行自然排序
def sort_filename(names):
    # 获取文件名列表的字符串长度分布
    name_lens = []
    for n in names:
        l = len(n)
        found = False
        for i in name_lens:
            if l == i:
                found = True
                break
        if not found:
            name_lens.append(l)

    # 从最短长度文件名开始进行排序
    new_list = []
    name_lens.sort()
    for i in name_lens:
        tmp_list = []
        for n in names:
            if len(n) == i:
                tmp_list.append(n)
        tmp_list.sort()
        for n in tmp_list:
            new_list.append(n)

    return new_list


def create_cbz(name, ziplist):
    zipname = '%s.cbz' % name
    print 'create', zipname, 'with', len(ziplist), 'files'
    if os.path.exists(zipname):
        print '  exist!'
        return
    args = ['zip', zipname]
    for i in ziplist:
        args.append(i)
    child = subprocess.Popen(args, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    stdout, stderr = child.communicate()
    if child.returncode:
        print stderr

def create_multi_cbz(prefix='test', order=1, end=False):
    files_per_cbz = 20
    lst = glob.glob('*.jpg')
    n = order
    c = 1
    ziplist = []
    last = ''
    for i in lst:
        if c > files_per_cbz:
            name = '%s%03d' % (prefix, n)
            create_cbz(name, ziplist)
            last = ziplist[-1]
            c = 1
            n += 1
            for l in ziplist:
                os.remove(l)
            ziplist = []

        if c <= files_per_cbz:
            ziplist.append(i)
            c += 1

    if end:
        name = '%s%03d' % (prefix, n)
        create_cbz(name, ziplist)
    else:
        print 'last processed:', last


############################################################

if __name__ == "__main__":
    print 'EBookHandy (C) 2002-2014 migrsoft'
    print 'Current folder: ' + os.getcwd()

