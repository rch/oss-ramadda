#!/bin/sh

#
#This script installs some base packages, Postgres and then RAMADDA
#

MYDIR=`dirname $0`
source "${MYDIR}/lib.sh"

do_mount() {
    header  "Volume Installation";
    echo "The database and the RAMADDA home directory will be installed on /mnt/ramadda"
    echo "We need to mount a volume as /mnt/ramadda"
    declare -a dirLocations=("/dev/sdb" )
    for i in "${dirLocations[@]}"
    do
	if [ -b "$i" ]; then
            askYesNo  "Do you want to mount the volume: $i "  "y"
            if [ "$response" == "y" ]; then
		MOUNT_DIR="$i"
		break;
            fi
	fi
    done

    ##/dev/xvdb       /mnt/ramadda   ext4    defaults,nofail        0       2
    while [ "$MOUNT_DIR" == "" ]; do
	ask  "Enter the volume to mount, e.g., /dev/xvdb  [<volume>|n] "  ""
	if [ "$response" == "" ] ||  [ "$response" == "n"  ]; then
            break;
	fi
	if [ -b $response ]; then
            MOUNT_DIR="$response"
            break;
	fi
	echo "Volume does not exist: $response"
    done

    echo "Mounting: $MOUNT_DIR"

    if [ "$MOUNT_DIR" != "" ]; then
	mntState=$( file -s $MOUNT_DIR );
	case $mntState in
	    *files*)
		echo "$MOUNT_DIR is already mounted";
		;;
	    *)
		echo "Mounting $BASE_DIR on $MOUNT_DIR"
		if [ ! -f /etc/fstab.bak ]; then
		    cp  /etc/fstab /etc/fstab.bak
		fi
		sed -e 's/.*$BASE_DIR.*//g' /etc/fstab | sed -e 's/.*added ramadda.*//g' > dummy.fstab
		mv dummy.fstab /etc/fstab
		printf "\n#added by ramadda installer.sh\n${MOUNT_DIR}   $BASE_DIR ext4 defaults,nofail   0 2\n" >> /etc/fstab
		askYesNo "Do you want to make the file system on ${MOUNT_DIR}?"  "y"
		if [ "$response" == "y" ]; then
		    mkfs -t ext4 $MOUNT_DIR
		fi
		mkdir $BASE_DIR
		mount $MOUNT_DIR $BASE_DIR
		mount -a
		;;
	esac
    fi
}


do_basedir() {
    dfltDir="";
    if [ -d "${USER_DIR}" ]; then
	dfltDir="${USER_DIR}/ramadda";
    fi

    if [ -d "/mnt/ramadda" ]; then
	dfltDir="/mnt/ramadda";
    fi

    while [ "$BASE_DIR" == "" ]; do
	ask   "Enter base directory: [$dfltDir]:" $dfltDir  "The base directory holds the repository and pgsql sub-directories"
	if [ "$response" == "" ]; then
            break;
	fi
	BASE_DIR=$response;
	break
    done
}





##do_basedir

if [ ! -d "$BASE_DIR" ]; then
    do_mount;
else
    echo "$BASE_DIR already mounted"
fi

mkdir -p $RAMADDA_HOME_DIR

tmpdir=`dirname $BASE_DIR`
permissions=$(stat -c %a $tmpdir)
if [ "$permissions" == "700" ]; then
    chmod 755 "$tmpdir"
fi


#echo "Installing wget"
#yum install -y wget > /dev/null
#yum install -y wget 

#java
echo "Installing Java"
yum install -y java
sudo /usr/sbin/alternatives --config java
sudo /usr/sbin/alternatives --config javac


askYesNo "Install postgres"  "y"
if [ "$response" == "y" ]; then
    installPostgres
fi



echo "Fixing the localhost name problem"
sed -e 's/HOSTNAME=localhost.localdomain/HOSTNAME=ramadda.localdomain/g' /etc/sysconfig/network> dummy.network
mv dummy.network /etc/sysconfig/network
sed -e 's/127.0.0.1   localhost localhost.localdomain/127.0.0.1 ramadda.localdomain ramadda localhost localhost.localdomain/g' /etc/hosts> dummy.hosts
mv dummy.hosts /etc/hosts






header  "RAMADDA Installation"
askYesNo "Download and install RAMADDA from Geode Systems"  "y"
if [ "$response" == "y" ]; then
    download_installer
    ask  "Where should RAMADDA be installed? currently: ${RUNTIME_DIR}"  ${RUNTIME_DIR}
    export RUNTIME_DIR=$response
    install_ramadda

    askYesNo "Install RAMADDA as a service"  "y"
    if [ "$response" == "y" ]; then
        printf "#!/bin/sh\n# chkconfig: - 80 30\n# description: RAMADDA repository\n\nsh ${SERVICE_SCRIPT} \"\$@\"\n" > ${SERVICE_DIR}/${SERVICE_NAME}
        chmod 755 ${SERVICE_DIR}/${SERVICE_NAME}
        chkconfig ${SERVICE_NAME} on
        printf "To run the RAMADDA service do:\nsudo service ${SERVICE_NAME} start|stop|restart\n"
	printf "Service script is: ${SERVICE_SCRIPT}\n"
    fi
fi




header "SSL Configuration";
printf "We need the public IP address to configure SSL\n"
read -p "Are you running in Amazon AWS? [y|n]: " response
if [ "$response" == "y" ]; then
    export MYIP=`curl http://169.254.169.254/latest/meta-data/public-ipv4 2>/dev/null`
    printf "OK, the public IP of this machine is ${MYIP}\n"
else
    export MYIP=`ifconfig | grep inet | grep cast | awk '/inet addr/{print substr($2,6)}'`
    if [ "$MYIP" == "" ]; then
	export MYIP=`ifconfig | grep inet | grep cast | awk '/.*/{print $2}'`
    fi
    read -p "Is this IP address correct - ${MYIP}?  [y|n]: " response
    if [ "$response" == "n" ]; then
	read -p "Enter the IP address: " tmpip
	export MYIP="${tmpip}"
    fi
fi


printf "A self-signed SSL certificate can be created for the IP address ${MYIP}\n";
printf "This will enable you to access your server securely but you will need to\n";
printf "add a real certificate or add other entries to the keystore for other domain names\n";
askYesNo "Generate keystore and enable SSL" "y"
if [ "$response" == "y" ]; then
    generate_keystore
fi


generate_install_password

service ${SERVICE_NAME} restart

header "Installation complete";
printf "RAMADDA is installed. \n\tRAMADDA home directory: ${RAMADDA_HOME_DIR}\n\tPostgres directory: ${PG_REAL_DIR}\n\tLog file: ${RUNTIME_DIR}/ramadda.log\n"
printf "Finish the configuration at https://${MYIP}/repository\n"
printf "The installation password is ${install_password}\n"



exit














