import sys
from pyjavaproperties import Properties
from string import Template
from build_utils import write_file, read_file

def get_version(branch):
    p = Properties()
    p.load(open("source/com.kms.katalon.application/about.mappings"))

    version = p['1']
    print("Version", version)

    skip_tests = p['4']
    print("Skip tests", skip_tests) 

    print("Branch", branch)

    is_release = ("release-" in branch) | ("-release-" in branch)
    print("Is release", is_release)

    is_beta = is_release & (".rc" in branch)
    print("Is beta", is_beta)

    with_update = (is_release is True) & (is_beta is False)
    print("With update", with_update)

    if is_release is True:

        if ((branch.endswith(version) | ("{0}.rc".format(version) in branch)) is False):
            print('Branch or version is incorrect.')
            raise ValueError('Branch or version is incorrect.')
            
        tag = branch.replace('release-', '')
    else:
        tag = "{0}.DEV".format(version)
    print("Tag", tag)

    s3_location = ""
    if is_beta is True:
        s3_location = "release-beta/{0}".format(tag)
    else:
        s3_location = tag
    
    run_tests = (is_release is True) & (skip_tests.lower() == 'false')

    variableTemplate = Template(
"""
#!/usr/bin/env bash

version=${version}
isRelease=${is_release}
isBeta=${is_beta}
withUpdate=${with_update}
tag=${tag}
s3Location=${s3_location}
runTests=${run_tests}
""")
    variable = variableTemplate.substitute(version = version, is_release = str(is_release).lower(), is_beta = str(is_beta).lower(), with_update = str(with_update).lower(), tag = tag, s3_location = s3_location, run_tests = str(run_tests).lower())
    write_file(file_path = "variable.sh", text = variable)
    print(read_file(file_path = "variable.sh"))

branch = sys.argv[1]
get_version(branch)
