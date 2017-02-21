#!/usr/bin/env python
# -*- coding: UTF-8 -*-

'''
Created on Mar 5, 2015

@author: lqp
'''

import os
import re

import os.path as pathUtil


bbLogRegPattern  = re.compile(r'(^\s*)(BBLog\.[^;]+;)', re.DOTALL | re.MULTILINE);
normalLogRegPattern  = re.compile(r'(^\s*)(Log\.[^;]+;)', re.DOTALL | re.MULTILINE);

workDir = '..';
srcFolder = ['app/src/main/java',
             'bbcomm/src/main/java',
             'comm/src/main/java',
             'feed/src/main/java',
             'p_history/src/main/java',
             'live_lib/src/main/java',
             'login/src/main/java',
             'p_classlive/src/main/java',
             'p_collect/src/main/java',
             'p_comp/src/main/java',
             'p_exercise/src/main/java',
             'p_guwen/src/main/java',
             'p_kefu/src/main/java',
             'p_live/src/main/java',
             'p_message/src/main/java',
             'p_photoselector/src/main/java',
             'p_scanword/src/main/java',
             'pay/src/main/java',
             'pluginbase/src/main/java',
             'skin/src/main/java',
             'p_user/src/main/java',
             'p_zxing/src/main/java',
             'p_credit/src/main/java'
             ];
processCount = 0

def listFiles(path, outList):
    files = os.listdir(path);
    
    for item in files:
        item = path + pathUtil.sep + item;
        
        if pathUtil.isdir(item):
            listFiles(item, outList);
        else:
            if pathUtil.splitext(item)[1] == '.java':
                outList.append(item)
    
    
    return

def processFile(fileName):
    srcFile = open(fileName, "r");
    content = srcFile.read()
    srcFile.close()
    
    global workDir
    needWrite = 0
    
    #replace BBlog
    groups  = bbLogRegPattern.findall(content);
    if len(groups) > 0:
        global processCount

        print 'purge BBLog in: ' + pathUtil.relpath(fileName, workDir);
        
        content = re.sub(bbLogRegPattern, r'\1//BBLog call replaced', content);
        
        needWrite = 1
        
    ##replace Log.x()
    groups  = normalLogRegPattern.findall(content);
    if len(groups) > 0:
        global processCount

        print 'purge Log.x in: ' + pathUtil.relpath(fileName, workDir);
        
        content = re.sub(normalLogRegPattern, r'\1//Log.x call replaced', content);
        
        needWrite = 1
        
    if needWrite:
        srcFile = open(fileName, "w");
        srcFile.write(content)
        srcFile.close()
        processCount = processCount + 1
        
    return

if __name__ == '__main__':
    holderList = []
    
    workDir = pathUtil.abspath(workDir)
    
    print 'start work at dir: ' + workDir

    for folder in srcFolder:
        listFiles(workDir + os.path.sep + folder, holderList);
    
    for item in holderList:
        processFile(item)
    
    print 'process complete: ' + str(processCount) + ' files changed'
    pass
