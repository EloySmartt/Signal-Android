#!/bin/sh

#replace SIGNAL_URL in app/build.gradle
#sed -i "s/chat.signal.org/chat.staging.smarttmessenger.com/g"

for dir in $(find . -type d -name "securesms"); do
    DELPATH=$dir
	echo "Dir found: $dir"
	
	NEWPATH=${dir//securesms/app}
	echo "1) Moving from ${dir} to ${NEWPATH}"
    mv $dir $NEWPATH
	dir=${NEWPATH}
    DELPATH=${DELPATH%/*}

	NEWPATH=${dir//thoughtcrime/smarttmessenger}
	echo "2) Moving from ${dir} to ${NEWPATH}"
    mkdir -p $NEWPATH
    cp -r $dir/* $NEWPATH
    #rm -R $dir
    dir=${NEWPATH}
    DELPATH=${DELPATH%/*}

	NEWPATH=${dir//org/com}
	echo "3) Moving from ${dir} to ${NEWPATH}"
    mkdir -p $NEWPATH
    cp -r $dir/* $NEWPATH
    
    rm -R $DELPATH
done;

for dir in $(find . -type d -name "thoughtcrime"); do
    DELPATH=$dir
	echo "Dir found: $dir"
	
	NEWPATH=${dir//thoughtcrime/smarttmessenger}
	echo "1) Moving from ${dir} to ${NEWPATH}"
    mv $dir $NEWPATH
	dir=${NEWPATH}
    DELPATH=${DELPATH%/*}

	NEWPATH=${dir//org/com}
	echo "2) Moving from ${dir} to ${NEWPATH}"
    mkdir -p $NEWPATH
    cp -r $dir/* $NEWPATH
    
    rm -R $DELPATH
done;
