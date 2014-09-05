#include <stdio.h>
#include <stdlib.h>

#define _UINT8 unsigned char

static _UINT8 JPEG_TAG[] = {
    0xD8, 0xD9, // SOI, EOI
    0xE0, 0xE1, 0xE2, 0xE3, 0xE4, 0xE5, 0xE6, 0xE7,
    0xE8, 0xE9, 0xEA, 0xEB, 0xEC, 0xED, 0xEE, 0xEF,
    0xDB, // DQT
    0xC0, // SOF0
    0xC4, // DHT
    0xDA, // SOS
    0
};

int is_jpeg_tag(_UINT8 tag)
{
    int i;
    for (i=0; JPEG_TAG[i]; i++) {
        if (JPEG_TAG[i] == tag)
            return 1;
    }
    return 0;
}

void test_read(char* fname)
{
    _UINT8 buf[8];
    size_t sz;
    int err = 0;
    long length;

    FILE* fp = fopen(fname, "r");
    if (fp == NULL) {
        printf("open %s failed!\n", fname);
        return;
    }

    err = 1;
    sz = fread(buf, 1, 2, fp);
    if (sz == 2 && buf[0] == 0xFF && buf[1] == 0xD8) {
        err = 0;
        printf("is jpeg\n");
    }

    if (!err) {
        while (1) {
            sz = fread(buf, 1, 2, fp);
            if (sz == 2 && buf[0] == 0xFF && is_jpeg_tag(buf[1])) {
                printf("TAG: %x ", buf[1]);
                fread(buf, 1, 2, fp);
                length = buf[0]; length <<= 8; length |= buf[1]; length -= 2;
                printf("LEN: %ld\n", length);
                fseek(fp, length, SEEK_CUR);
            }
            else {
                printf("now: %x %x\n", buf[0], buf[1]);
                break;
            }
        }
    }

    fclose(fp);
}

int verify_jpeg(char* pic)
{
    _UINT8 buf[4];
    FILE* fp = fopen(pic, "r");
    if (fp == NULL)
        return 0;

    fread(buf, 1, 2, fp);
    fseek(fp, -2L, SEEK_END);
    fread(&buf[2], 1, 2, fp);
    //printf("%x %x %x %x\n", buf[0], buf[1], buf[2], buf[3]);
    fclose(fp);
    return (buf[0] == 0xff && buf[1] == 0xd8 && buf[2] == 0xff && buf[3] == 0xd9);
}


long get_length(FILE* fp)
{
    long length;
    fseek(fp, 0L, SEEK_END);
    length = ftell(fp);
    rewind(fp);
    return length;
}

#define BUFFER_SIZE 1024 * 32

int write_pic_a(FILE* dst, char* pic)
{
    _UINT8* buf = (_UINT8*) malloc(BUFFER_SIZE);
    FILE* fp;
    if (buf == NULL)
        return -1;

    fp = fopen(pic, "r");
    if (fp != NULL) {
        size_t read;
        long length = get_length(fp) - 2;

        while (length > 0) {
            read = (length >= BUFFER_SIZE) ? BUFFER_SIZE : length;
            length -= read;
            fread(buf, 1, read, fp);
            fwrite(buf, 1, read, dst);
        }

        fclose(fp);
    }
    free(buf);
    return 0;
}

void write_pic_b(FILE* dst, char* pic)
{
    _UINT8* buf = (_UINT8*) malloc(BUFFER_SIZE);
    FILE* fp;
    if (buf == NULL)
        return;

    fp = fopen(pic, "r");
    if (fp != NULL) {
        size_t read, mark_len;
        long length = get_length(fp);
        _UINT8 mark[6];
        int i = 0;

        while (length > 0) {
            read = (length >= BUFFER_SIZE) ? BUFFER_SIZE : length;
            length -= read;
            fread(buf, 1, read, fp);

            mark[0] = 0xFF;
            mark[1] = 0xEF;

            mark_len = 2 + 1 + read + 4;
            mark[2] = mark_len >> 8 & 0xff;
            mark[3] = mark_len & 0xff;
            mark[4] = ++i & 0xff;

            fwrite(mark, 1, 5, dst);
            fwrite(buf, 1, read, dst);

            mark_len += 2;
            mark[0] = 0xEE;
            mark[1] = 0xEE;
            mark[2] = mark_len >> 8 & 0xff;
            mark[3] = mark_len & 0xff;
            fwrite(mark, 1, 4, dst);

       }

        fclose(fp);
    }

    free(buf);
    return;
}

void hide_pic(char* picA, char* picB)
{
    FILE* dst = NULL;

    if (!verify_jpeg(picA)) {
        printf("%s is error\n", picA);
        return;
    }
    if (!verify_jpeg(picB)) {
        printf("%s is error\n", picB);
        return;
    }

    dst = fopen("new.jpg", "w");
    if (dst != NULL) {
        if (write_pic_a(dst, picA) == 0) {
            write_pic_b(dst, picB);
            fputc(0xff, dst);
            fputc(0xd9, dst);
        }
        fclose(dst);
    }
}

int main(int argc, char** argv)
{
    if (argc == 2) {
//        test_read(argv[1]);
    }
    else if (argc == 3) {
        hide_pic(argv[1], argv[2]);
    }

    return 0;
}

