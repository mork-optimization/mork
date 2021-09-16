#!/bin/bash
###############################################################################
# This script is the command that is executed every run.
# Check the examples in examples/
#
# This script is run in the execution directory (execDir, --exec-dir).
#
# PARAMETERS:
# $1 is the candidate configuration number
# $2 is the instance ID
# $3 is the seed
# $4 is the instance name
# The rest ($* after `shift 4') are parameters to the run
#
# RETURN VALUE:
# This script should print one numerical value: the cost that must be minimized.
# Exit with 0 if no error, with 1 in case of error
###############################################################################
error() {
    echo "`TZ=UTC date`: $0: error: $@"
    exit 1
}


CONFIG_ID=$1
INSTANCE_ID=$2
SEED=$3
INSTANCE=$4
shift 4 || error "Not enough parameters"
CONFIG_PARAMS=$*

STDOUT=c${CONFIG_ID}-${INSTANCE_ID}-${SEED}.stdout
STDERR=c${CONFIG_ID}-${INSTANCE_ID}-${SEED}.stderr

# If the program just prints a number, we can use 'exec' to avoid
# creating another process, but there can be no other commands after exec.
#exec $EXE ${FIXED_PARAMS} -i $INSTANCE --seed ${SEED} ${CONFIG_PARAMS}
# exit 1
#
# Otherwise, save the output to a file, and parse the result from it.
# (If you wish to ignore segmentation faults you can use '{}' around
# the command.)

java \
-Dirace.worker=true \
-Dspring.main.web-application-type=none \
-Dseed="${SEED}" \
-Dinstances.path.default="${INSTANCE}" \
-jar target/*.jar ${CONFIG_PARAMS} \
 1> ${STDOUT} 2> ${STDERR}


# $EXE ${INSTANCE} ${FIXED_PARAMS} --seed ${SEED} ${CONFIG_PARAMS}
#echo "Executable: ${EXE}"
#echo "Instance: ${INSTANCE}"
#echo "Fixed params (IGNORED): ${FIXED_PARAMS}"
#echo "SEED: ${SEED}"
#echo "Variable parameters: ${CONFIG_PARAMS}"



