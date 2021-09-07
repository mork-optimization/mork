# $EXE ${INSTANCE} ${FIXED_PARAMS} --seed ${SEED} ${CONFIG_PARAMS}
echo "Executable: ${EXE}"
echo "Instance: ${INSTANCE}"
echo "Fixed params (IGNORED): ${FIXED_PARAMS}"
echo "SEED: ${SEED}"
echo "Variable parameters: ${CONFIG_PARAMS}"

java -Dspring.main.web-application-type=none -Dsolver.irace=true -Dseed="${SEED}" -Dinstances.path.default="${INSTANCE}" -jar target/*.jar ${CONFIG_PARAMS}