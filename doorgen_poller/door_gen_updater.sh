#!/bin/sh

cd /home/user/soft/doorgen_poller
java -cp  "../java/*" com.fdt.doorgen.key.pooler.DoorgenUpdaterRunner config.ini
