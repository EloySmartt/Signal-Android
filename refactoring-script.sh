#!/bin/sh


#### DEFINE VARIABLES
NEW_SIGNAL_URL=chat.smarttmessenger.com
NEW_SIGNAL_URL_STAGING=chat.staging.smarttmessenger.com

NEW_SIGNAL_CAPTCHA_URL=https://captchas.smarttmessenger.com/registration/generate.html
NEW_SIGNAL_CAPTCHA_URL_STAGING=https://captchas.smarttmessenger.com/staging/registration/generate.html

NEW_RECAPTCHA_PROOF_URL=https://captchas.smarttmessenger.com/challenge/generate.html
NEW_RECAPTCHA_PROOF_URL_STAGING=https://captchas.smarttmessenger.com/staging/challenge/generate.html

NEW_UNIDENTIFIED_SENDER_TRUST_ROOT=BZOR67ODZ8N5+djGOeS7CvRuPeupe6q4mvsfJSwthKRK-asdf
NEW_UNIDENTIFIED_SENDER_TRUST_ROOT_STAGING=BZOR67ODZ8N5+djGOeS7CvRuPeupe6q4mvsfJSwthKRK-asdf

NEW_PACKAGE_NAME=com.smarttmessenger.app

NDK_BUILD_PATH=/Users/eloy/Library/Android/sdk/ndk/28.0.12433566/build/ndk-build


#### build.gradle replacements
echo "Replacing SIGNAL_URL in app/build.gradle"
sed -i '' "s/chat.signal.org/$NEW_SIGNAL_URL/g" ./app/build.gradle.kts
sed -i '' "s/chat.staging.signal.org/$NEW_SIGNAL_URL_STAGING/g" ./app/build.gradle.kts
echo ""

echo "Replacing SIGNAL_CAPTCHA_URL in app/build.gradle"
sed -i '' "s#https://signalcaptchas.org/registration/generate.html#$NEW_SIGNAL_CAPTCHA_URL#g" ./app/build.gradle.kts
sed -i '' "s#https://signalcaptchas.org/staging/registration/generate.html#$NEW_SIGNAL_CAPTCHA_URL_STAGING#g" ./app/build.gradle.kts
echo ""

echo "Replacing RECAPTCHA_PROOF_URL in app/build.gradle"
sed -i '' "s#https://signalcaptchas.org/challenge/generate.html#$NEW_RECAPTCHA_PROOF_URL#g" ./app/build.gradle.kts
sed -i '' "s#https://signalcaptchas.org/staging/challenge/generate.html#$NEW_RECAPTCHA_PROOF_URL_STAGING#g" ./app/build.gradle.kts
echo ""

echo "Replacing UNIDENTIFIED_SENDER_TRUST_ROOT in app/build.gradle"
sed -i '' "s/BXu6QIKVz5MA8gstzfOgRQGqyLqOwNKHL6INkv3IHWMF/$NEW_UNIDENTIFIED_SENDER_TRUST_ROOT/g" ./app/build.gradle.kts
sed -i '' "s/BbqY1DzohE4NUZoVF+L18oUPrK3kILllLEJh2UnPSsEx/$NEW_UNIDENTIFIED_SENDER_TRUST_ROOT_STAGING/g" ./app/build.gradle.kts
echo ""


#### JNI files renaming
echo "Renaming JNI files"
NEW_PACKAGE_NAME_UNDERSCORED="${NEW_PACKAGE_NAME//./_}"
mv app/jni/utils/org_thoughtcrime_securesms_util_FileUtils.cpp app/jni/utils/${NEW_PACKAGE_NAME_UNDERSCORED}_util_FileUtils.cpp
mv app/jni/utils/org_thoughtcrime_securesms_util_FileUtils.h app/jni/utils/${NEW_PACKAGE_NAME_UNDERSCORED}_util_FileUtils.h
echo ""


#### Renaming dir structure
IFS='.' read -ra DIR_COMPONENTS <<< "$NEW_PACKAGE_NAME"
echo "Renaming dirs /org/thoughtcrime/securesms to /${DIR_COMPONENTS[0]}/${DIR_COMPONENTS[1]}/${DIR_COMPONENTS[2]}"

for dir in $(find . -type d -name "securesms"); do
    DELPATH=$dir
	echo "Renaming $dir"
	
	NEWPATH=${dir//securesms/${DIR_COMPONENTS[2]}}
    mv $dir $NEWPATH
	dir=${NEWPATH}
    DELPATH=${DELPATH%/*}

	NEWPATH=${dir//thoughtcrime/${DIR_COMPONENTS[1]}}
    mkdir -p $NEWPATH
    cp -r $dir/* $NEWPATH
    dir=${NEWPATH}
    DELPATH=${DELPATH%/*}

	NEWPATH=${dir//org/${DIR_COMPONENTS[0]}}
    mkdir -p $NEWPATH
    cp -r $dir/* $NEWPATH
    
    rm -R $DELPATH
done;
echo ""

echo "Renaming dirs /org/thoughtcrime to /${DIR_COMPONENTS[0]}/${DIR_COMPONENTS[1]}"

for dir in $(find . -type d -name "thoughtcrime"); do
    DELPATH=$dir
	echo "Renaming $dir"
	
	NEWPATH=${dir//thoughtcrime/${DIR_COMPONENTS[1]}}
    mv $dir $NEWPATH
	dir=${NEWPATH}
    DELPATH=${DELPATH%/*}

	NEWPATH=${dir//org/${DIR_COMPONENTS[0]}}
    mkdir -p $NEWPATH
    cp -r $dir/* $NEWPATH
    
    rm -R $DELPATH
done;
echo ""


#### Search and replace
echo "Replacing org.thoughtcrime.securesms for $NEW_PACKAGE_NAME"
grep -lr 'org.thoughtcrime.securesms' . | xargs sed -i '' "s#org.thoughtcrime.securesms#$NEW_PACKAGE_NAME#g"
exit 1

NEW_PACKAGE_NAME_SHORT=`cut -d "." -f 1,2 <<< $NEW_PACKAGE_NAME`
echo "Replacing org.thoughtcrime for $NEW_PACKAGE_NAME_SHORT"
grep "org.thoughtcrime" . -lr | xargs sed -i "" -e "s#org.thoughtcrime#$NEW_PACKAGE_NAME_SHORT#g"
exit 1

echo "Replacing org_thoughtcrime_securesms for $NEW_PACKAGE_NAME_UNDERSCORED"
grep "org_thoughtcrime_securesms" . -lr | xargs sed -i "" "s#org_thoughtcrime_securesms#$NEW_PACKAGE_NAME_UNDERSCORED#g"
exit 1

echo "Replacing org/thoughtcrime/securesms for ${DIR_COMPONENTS[0]}/${DIR_COMPONENTS[1]}/${DIR_COMPONENTS[2]}"
# grep "org/thoughtcrime/securesms" . -lr | xargs sed -i "" "s#org/thoughtcrime/securesms#${DIR_COMPONENTS[0]}/${DIR_COMPONENTS[1]}/${DIR_COMPONENTS[2]}#g"

#### Rebuild config files
# NDK and CMake are needed -> https://developer.android.com/studio/projects/install-ndk

export NDK_PROJECT_PATH=`pwd`/app
$NDK_BUILD_PATH


#### Setting up Firebase


#### whisper.store