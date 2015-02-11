# I know this is so stupid

cd Frontend && \
./publish.sh && \
cd .. && \
cd Eval && \
sbt publish && \
cd .. && \
cd Backend && \
sbt publish && \
cd .. && \
cd Plugin && \
sbt publish

# cd Frontend && \
# sbt publish && \
# cd .. && \
# cd Eval && \
# sbt publish && \
# cd .. && \
# cd Backend && \
# sbt publish && \
# cd .. && \
# cd Plugin && \
# sbt publish
