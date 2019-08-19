source ~/.bashrc

PROJECT_FOLDER=$1

cd $PROJECT_FOLDER
cp -rf . /opt/ace/project
cd /opt/ace/project/build/mcp/SoapClasses
rm -f *.d
rm -f *.o
cd /opt/ace/project/build/mcp
rm -f *.d
rm -f *.o
cd /opt/ace/project/build
rm -f *.d;
rm -f *.o;
make clean;
make -j4 all;
