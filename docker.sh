docker run --name ES -d elasticsearch:2.4.4-alpine

docker run --name recogito-postgres -e POSTGRES_USER=recogito -e POSTGRES_PASSWORD=recogito -d kiasaki/alpine-postgres

docker run --rm -it -p 9010:9000 --link ES:ES --link recogito-postgres:postgres -v "$PWD":/root/recogito2 -v ~/.activator:/root/.activator -v ~/.ivy2_play:/root/.ivy2 -v ~/.sbt:/root/.sbt cignoir/play-scala-alpine ash

# then from the command line run
# cd ~
# ash activator.sh

# then open http://localhost:9100 in browser
