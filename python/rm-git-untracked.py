#!/usr/bin/python

import string
import os
import sys
import shutil


FILE_NAME = 'UNTRACKED'

def remove_untracked(fname):

    f = open(fname, 'r')
    lst = f.readlines()
    f.close()

    start = False
    for n in lst:
        n = string.strip(n)

        if string.find(n, 'Untracked files') > 0:
            start = True
            continue

        if start and n[0] == '#':
            n = string.strip(n[1:])

            if len(n) == 0:
                continue
            if n[0] == '(':
                continue

            print '>>>', n
            if n[-1] == '/':
                shutil.rmtree(n)
            else:
                os.remove(n)


if __name__ == '__main__':

    if os.path.exists(FILE_NAME):
        remove_untracked(FILE_NAME)
    else:
        print 'Usage:'
        print '1. get untracked files list'
        print '   git status >', FILE_NAME
        print '2. remove line from', FILE_NAME, 'which you do not need remove'
        print '3. run this script'

