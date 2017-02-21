#!/usr/bin/env python
#coding:utf-8

'''
Created on Sep 9, 2016

@author: xwang

更改插件中versionCode和versionName
包括build.gradle和androidmanifest.xml
先把target_versionCode和target_versionName改成需要的数值后再运行脚本
'''

import os 
import string
import sys

target_versionCode = '1004000'
target_versionName = '"1.4.0"'
plugin_arrs = ['p_classlive', 'p_collect', 'p_comp', 'p_exercise', 'p_guwen', 'p_kefu', 'p_live', 'p_message', 'p_photoselector', 'p_scanword', 'p_history', 'p_user', 'p_zxing', 'p_credit'];
base_dir = '../'


if __name__=='__main__':
    #######更改插件目录中build.gradle的数值
    print '------start to modify build.gradle in plugin folder-------'
    for pluginName in plugin_arrs:
        print '---current plugin----' + pluginName
        os.chdir(base_dir + pluginName)
        with open("build.gradle", "r") as f:
            lines = f.readlines()

        spaceSplitedProps = ('versionCode',
                             'versionName')
        i = 0
        for line in lines:
            
            line = line.strip()
            if (len(line) == 0):
                i = i + 1
                continue;

            for propName in spaceSplitedProps:
                if propName in line:
                    if (propName == 'versionCode'):
                        lines[i] = '        versionCode ' + target_versionCode + '\n'
                    if (propName == 'versionName'):
                        lines[i] = '        versionName ' + target_versionName + '\n'

            i = i + 1
        fl=open('build.gradle', 'w')
        for ii in lines:
            fl.write(ii)
        fl.close()

    ########更改插件目录中AndroidManifest.xml的数值
    print '------start to modify AndroidManifest.xml in plugin folder-------'
    j = 0
    for pluginName in plugin_arrs:
        i = 0
        print '---current plugin----' + pluginName
        print os.getcwd()
        if j == 0:
            os.chdir(base_dir + pluginName + '/src/main')
        else:
            os.chdir('../../../' + pluginName + '/src/main')
        with open("AndroidManifest.xml", "r") as f:
            lines = f.readlines()

        spaceSplitedProps = ('versionCode',
                             'versionName')

        for line in lines:
            
            line = line.strip()
            if (len(line) == 0):
                i = i + 1
                continue;

            for propName in spaceSplitedProps:
                if propName in line:
                    if (propName == 'versionCode'):
                        lines[i] = '    android:versionCode="' + target_versionCode + '"\n'
                    if (propName == 'versionName'):
                        lines[i] = '    android:versionName=' + target_versionName + '>\n'

            i = i + 1
        fl=open('AndroidManifest.xml', 'w')
        for ii in lines:
            fl.write(ii)
        fl.close()
        j = j + 1



