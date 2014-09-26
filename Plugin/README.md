## Publish Docker Image

seq(kataDockerSettings: _*)

```
sbt kata:docker // << error here
cd target/docker
sudo docker build -t="masseguillaume/scalakata:0.8.0" .
sudo docker push masseguillaume/scalakata:0.8.0
```
