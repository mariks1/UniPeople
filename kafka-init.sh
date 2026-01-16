#!/bin/bash
set -e

echo "Waiting for Kafka..."
until kafka-topics --bootstrap-server kafka1:9092 --list >/dev/null 2>&1; do
  echo "Kafka not ready yet..."
  sleep 2
done

kafka-topics --bootstrap-server kafka1:9092 --create --if-not-exists --topic leave.events --partitions 3 --replication-factor 3
kafka-topics --bootstrap-server kafka1:9092 --create --if-not-exists --topic employee.events --partitions 3 --replication-factor 3
kafka-topics --bootstrap-server kafka1:9092 --create --if-not-exists --topic duty.events --partitions 3 --replication-factor 3
kafka-topics --bootstrap-server kafka1:9092 --create --if-not-exists --topic notification.dlq --partitions 3 --replication-factor 3

echo "Topics created"
