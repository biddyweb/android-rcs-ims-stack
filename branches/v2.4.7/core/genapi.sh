#!/bin/bash
cd $RCS_STACK_PATH/core/bin/classes
jar cf $RCS_STACK_PATH/core/libs/rcs_api.jar @$RCS_STACK_PATH/core/jarfiles.txt
