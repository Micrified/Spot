echo CONFIG_FILE=/usr/local/etc/rabbitmq/rabbitmq.config > /usr/local/etc/rabbitmq/rabbitmq-env.conf
echo NODE_IP_ADDRESS=$1 >> /usr/local/etc/rabbitmq/rabbitmq-env.conf
echo NODENAME=rabbit@localhost >> /usr/local/etc/rabbitmq/rabbitmq-env.conf
