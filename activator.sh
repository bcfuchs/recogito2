activator -v run -Ddb.default.url="jdbc:postgresql://"$POSTGRES_PORT_5432_TCP_ADDR"/recogito" -Des.port=$ES_PORT_9300_TCP_PORT -Des.host=$ES_PORT_9300_TCP_ADDR
