# Makefile for libhidpunk.jnilib

# MIT Media Lab
# Cognitive Machines
# Philip DeCamp, June 2008

PLATFORM := osx
MY_NAME := libhidpunk

export CC := gcc
export CXX := g++
export JAVAC := javac

export ERASE_FILE ?= rm -f
export ERASE_DIR ?= rm -r
export MAKE_DIR ?= mkdir -p
export COPY_FILE ?= cp
export MAKE_LIB ?= g++ -dynamiclib -o
export INDEX_LIB ?= @echo
export MAKE ?= make

export SCRATCH_DIR := scratch
export LIB_PATH := scratch
export LIB_OUTPUT := ../../lib

export MACOSX_DEPLOYMENT_TARGET ?= 10.6
export ARCH ?= -arch x86_64
export SYS_ROOT ?= /Applications/Xcode.app/Contents/Developer/Platforms/MacOSX.platform/Developer/SDKs/MacOSX10.9.sdk


DEBUG_CXXFLAGS := -D_REENTRANT -dynamic -fPIC -fno-common -Wall -g3
DEBUG_LDFLAGS := -D_REENTRANT -dynamic -fPIC -fno-common -Wall -g3

RELEASE_CXXFLAGS := -D_REENTRANT -dynamic -fPIC -fno-common -Wall -Wstrict-aliasing=0 -O3 -force_cpusubtype_ALL
RELEASE_CXXFLAGS := $(RELEASE_CXXFLAGS) -isysroot $(SYS_ROOT) $(ARCH)
RELEASE_CXXFLAGS := $(RELEASE_CXXFLAGS) -mmacosx-version-min=$(MACOSX_DEPLOYMENT_TARGET)

RELEASE_LDFLAGS := -D_REENTRANT -dynamic -fPIC -Wall
RELEASE_LDFLAGS := $(RELEASE_LDFLAGS) -Wl,-syslibroot,$(SYS_ROOT) $(ARCH)
RELEASE_LDFLAGS := $(RELEASE_LDFLAGS) -mmacosx-version-min=$(MACOSX_DEPLOYMENT_TARGET)

export CXXFLAGS := $(RELEASE_CXXFLAGS)
export LDFLAGS := $(RELEASE_LDFLAGS)

OUTPUT_DIR := ../../lib
FINAL_TARGET := $(OUTPUT_DIR)/$(MY_NAME).jnilib

SOURCE_DIRS := hidpunk
LIB_NAMES := 
INCLUDE_NAMES := 
EXT_INCLUDES := 
EXT_LIBS := -lc
FRAMEWORKS := -framework CoreFoundation -framework Carbon -framework IOKit -framework JavaVM

SOURCES := $(foreach dir, $(SOURCE_DIRS), $(wildcard $(dir)/*.cpp))
C_SOURCES := $(foreach dir, $(SOURCE_DIRS), $(wildcard $(dir)/*.c))
HEADERS := $(foreach dir, $(SOURCE_DIRS), $(wildcard $(dir)/*.h))

INCLUDES := $(EXT_INCLUDES)
INCLUDES := $(INCLUDES) $(foreach dir, $(SOURCE_DIRS), -I$(dir))
INCLUDES := $(INCLUDES) $(foreach dir, $(INCLUDE_NAMES), -I$(LIB_PATH)/$(dir))
INCLUDES := $(INCLUDES) $(foreach dir, $(LIB_NAMES), -I$(LIB_PATH)/lib$(dir))

LIBS := $(EXT_LIBS) $(FRAMEWORKS)

OBJECTS := $(patsubst %.cpp, $(SCRATCH_DIR)/%.cpp.o, $(SOURCES))
OBJECTS := $(OBJECTS) $(patsubst %.c, $(SCRATCH_DIR)/%.c.o, $(C_SOURCES))


.PHONY: all
all: _make_some_room_ _make_libs_ $(FINAL_TARGET)


$(FINAL_TARGET): $(OBJECTS)
	$(MAKE_DIR) $(OUTPUT_DIR)
	$(MAKE_LIB) $@ $(LDFLAGS) $+ $(foreach lib, $(LIB_NAMES), $(wildcard $(LIB_PATH)/lib$(lib)/*.o)) $(LIBS)
	$(INDEX_LIB) $@


$(SCRATCH_DIR)/%.cpp.o: %.cpp
	$(MAKE_DIR) $(dir $@)
	$(CXX) -o $@ -c $(CXXFLAGS) $(INCLUDES) $< 


$(SCRATCH_DIR)/%.c.o: %.c
	$(MAKE_DIR) $(dir $@)
	$(CC) -o $@ -c $(CXXFLAGS) $(INCLUDES) $<


$(OUTPUT_DIR)/%.h:
	$(MAKE_DIR) $(OUTPUT_DIR)
	$(COPY_FILE) $*.h $(OUTPUT_DIR)/$(notdir $*.h)


.PHONY: _make_libs_
_make_libs_:
	@echo


.PHONY: _make_some_room_
_make_some_room_:
	@echo
	@echo
	@echo Making $(MY_NAME)
	@echo
	@echo $(OBJECTS)


.PHONY: clean
clean:
	$(ERASE_DIR) $(SCRATCH_DIR)


.PHONY: _clean_me_/%
_clean_me_/%:
	$(ERASE_FILE) $*

