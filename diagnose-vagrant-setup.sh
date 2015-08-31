#!/bin/bash
# TODO: validate slave IPs match too
# TODO: connectivity between servers
CURDIR=$(dirname "$0")
EXPECTED_MASTER_IP="10.10.4.2"
RED='\033[0;31m'
GREEN='\033[0;32m'
NC='\033[0m' # No Color
HOSTS_LIST_CACHE=""

get_hosts(){
    if [ "x$HOSTS_LIST_CACHE" = "x" ];
    then
       HOSTS_LIST_CACHE=$(vagrant hosts list)
    fi
    echo "${HOSTS_LIST_CACHE}"
}

success(){
    echo -e "${GREEN}Success: ${NC}" $1
}

failed(){
    echo -e "${RED}Failed: ${NC}" $1 "Result[" $2 "]" "Expected[" $3 "]"
}

get_actual_host_ip(){
    HOST_NAME=$1
    vagrant ssh $HOST_NAME <<EOF | grep -A1 eth1 | perl -lne 'print $1 if /inet addr:(\S+)/'
ifconfig
EOF
}

get_slaves_count(){
    if [ "x$MESOS_SLAVES" != "x" ];
    then
        return $MESOS_SLAVES;
    else
        echo $(grep MESOS_SLAVES $CURDIR/Vagrantfile | tr -d '[:alpha:][:punct:][:blank:]');
    fi
}

validate_master_ip(){
    master_ip=$(get_hosts | grep master | cut -f1)
    echo $master_ip | grep $EXPECTED_MASTER_IP && success "Valid Master IP $EXPECTED_MASTER_IP" || failed "Invalid Master IP" $master_ip $EXPECTED_MASTER_IP
}

validate_slave_counts(){
    expected_slaves_count=$(get_slaves_count)
    total_slaves=$(get_hosts | grep slave | wc -l)
    echo $total_slaves | grep $expected_slaves_count && success "Valid number of slaves $expected_slaves_count" || failed "Slave counts don't match" $total_slaves $expected_slaves_count
}


validate_master_ip_matches_with_vagrantfile(){
    vagrant_master_ip=$(get_hosts | grep master | cut -f1)
    actual_master_ip=$(get_actual_host_ip master)

    echo $actual_master_ip | grep $EXPECTED_MASTER_IP && success "Master IP in the machine matches with $EXPECTED_MASTER_IP" || failed "Invalid Master IP for the machine than what vagrant expects" $actual_master_ip $EXPECTED_MASTER_IP
}





validate_master_ip
validate_slave_counts
validate_master_ip_matches_with_vagrantfile
