# -*- mode: ruby -*-
# vi: set ft=ruby :

vagrant_slaves = ENV.fetch('MESOS_SLAVES', 2).to_i

Vagrant.configure(2) do |config|
  config.vm.box = "edpaget/mesos"
  config.vm.box_check_update = false

  config.vm.provider "virtualbox" do |vb|
    vb.memory = "2048"
  end

  config.vm.define "master" do |master|
    master.vm.network "private_network", ip: "10.10.4.2"
    master.vm.hostname = "master"

    master.vm.provision "shell", inline: <<-SHELL
      sed -i.bak '9,10d' /etc/network/interfaces && sudo ifdown eth1 && sudo ifup eth1
      echo "10.10.4.2" > /etc/mesos-master/ip
      rm -f /etc/init/zookeeper.override
      service zookeeper restart

      rm -f /etc/init/mesos-master.override
      service mesos-master restart

      rm -f /etc/init/marathon.override
      service marathon restart

      wget -q -O /usr/local/bin/lein https://raw.githubusercontent.com/technomancy/leiningen/stable/bin/lein
      chmod +x /usr/local/bin/lein

    SHELL
  end

  vagrant_slaves.times do |n|
    config.vm.define "slave_#{n}" do |slave|
      slave.vm.network "private_network", ip: "10.10.4.1#{n}"
      slave.vm.hostname = "slave-#{n}"
      slave.vm.provision "shell", inline: <<-SHELL
        sed -i.bak '9,10d' /etc/network/interfaces && sudo ifdown eth1 && sudo ifup eth1
        echo "zk://10.10.4.2:2181/mesos" > /etc/mesos/zk
        echo "10.10.4.1#{n}" > /etc/mesos-slave/ip
        echo "mesos,docker" > /etc/mesos-slave/containerizers

        rm -f /etc/init/mesos-slave.override
        service mesos-slave restart

        wget -q -O /usr/local/bin/lein https://raw.githubusercontent.com/technomancy/leiningen/stable/bin/lein
        chmod +x /usr/local/bin/lein
      SHELL
    end
  end
end
