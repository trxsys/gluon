#! /bin/python

import sys

def parse_module(line):
    start=line.find('.<')
    end=line[start:].find(':')

    return line[start+2:start+end]

def parse_method(line):
    start=line.find('.<')
    end=line.find('>')

    method=line[start+2:end]
    method=method[:method.find('(')]
    method=method.split(' ')[-1]

    return method

def main(icf_out):
    done={}

    fo=open(icf_out,'r')
    foout=open('icf_tomcat.sh','w')

    foout.write('#! /bin/bash\n\n')
    foout.write('cd ..\n\n')
    foout.write('rm -f tomcat_icf_use\n')

    for line in fo:
        if 'found by ICfinder-USE' in line:
            sfirst=fo.readline()
            ssecond=fo.readline()

            if parse_module(sfirst) != parse_module(ssecond):
                # print('DIFF MODULES')
                continue

            module=parse_module(sfirst)
            method1=parse_method(sfirst)
            method2=parse_method(ssecond)

            if module == line.split(' ')[-1].strip():
                # print('INSIDE MODULE')
                continue

            if (module,method1,method2) in done:
                # print('REPEATED')
                continue

            notfound=['setMessage',
                      'createStreams',
                      'getChildren',
                      'getId',
                      'getPolyType',
                      'getChildren',
                      'getText',
                      'getPolyType',
                      'getAttributeMap',
                      'setDir',
                      'getSoTimeout',
                      'start',
                      'addBuildListener',
                      'add']

            if method1 in notfound or method2 in notfound:
                # print('not found')
                continue

            print(module+' '+method1+' '+method2)
            # print(method1+' '+method2)

            done[(module,method1,method2)]=True

            foout.write('\n')

            foout.write('./gluon.sh --timeout 45 -t -p -s -y -r '
                        +'--classpath ../tomcat/output/classes \\\n')
            foout.write('  --module '+module+' \\\n')
            foout.write('  --contract "'+method1+' '+method2+'" \\\n')

            foout.write('  org.apache.catalina.startup.Bootstrap >> tomcat_icf_use\n')

    foout.close()
    fo.close()

    return 0

if len(sys.argv) != 2:
    print("Syntax: "+sys.argv[0]+" <icf_out>")
    exit(-1)

exit(main(sys.argv[1]))
