# I know this is so stupid

# cd Frontend && \
# ./publish.sh && \
# cd .. && \
# cd Eval && \
# sbt publishLocal && \
# cd .. && \
# cd Backend && \
# sbt publishLocal && \
# cd .. && \
# cd Plugin && \
# sbt publishLocal

cd Frontend && \
sbt publish && \
cd .. && \
cd Eval && \
sbt publish && \
cd .. && \
cd Backend && \
sbt publish && \
cd .. && \
cd Plugin && \
sbt publish
