#------------------------------------------------------------------------
#
# Beam-ACO for Travelling Salesman Problem with Time Windows.

VERSION = svn$(SVN_REV)

# ---------------------------------------------------------------------
#
#                       Copyright (c) 2008-2015
#                  Christian Blum <christian.blum@ehu.es>
#             Manuel Lopez-Ibanez <manuel.lopez-ibanez@ulb.ac.be>
#
# This program is free software (software libre); you can redistribute
# it and/or modify it under the terms of the GNU General Public License
# as published by the Free Software Foundation; either version 2 of the
# License, or (at your option) any later version.
#
# This program is distributed in the hope that it will be useful, but
# WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
# General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program; if not, you can obtain a copy of the GNU
# General Public License at: http://www.gnu.org/licenses/gpl.html
#
#------------------------------------------------------------------------

DEBUG = 1
OBJECTIVE=TOURCOST
NUMBER_TYPE=INT
#NUMBER_TYPE=DOUBLE
#OBJECTIVE=MAKESPAN
PROBLEMFLAGS = -DMINIMISE_$(OBJECTIVE) -DNUMBER_TYPE_IS=NUMBER_TYPE_$(NUMBER_TYPE)


TARGET=beamaco_tsptw
EXE=$(TARGET)_$(OBJECTIVE)

RNODE=iridiacluster
march=native
OPTIMISE = -O0
OPTIMISE = -Ofast -ffast-math -flto
#OPTIMISE = -O3 -ftree-vectorize -ffast-math -ftree-vectorizer-verbose=7 

BASE_DIR=../../
LIBMISC_DIR := $(BASE_DIR)/libmisc
LIBMISC_SRC := $(LIBMISC_DIR)/current
PWD	:= $(shell /bin/pwd)

ifneq ($(DEBUG),0)
CDEBUG = -g3 -DMALLOC_CHECK_=3
else
CDEBUG = -DNDEBUG
endif

override CXXFLAGS += -Wall -Wextra \
	-DVERSION=\"$(VERSION)\" -DDEBUG=$(DEBUG) $(CDEBUG) $(PROBLEMFLAGS) \
	-march=$(march) -DMARCH=\"$(MARCH)\" $(OPTIMISE) \
	-I $(LIBMISC_SRC) # -Weffc++

EXES := localsearch_tsptw beamaco_tsptw firstimprov_tsptw gvns_tsptw
SOURCES := ant.cpp  beam_element.cpp  Random.cc  Timer.cc  tsptw_solution.cpp
HEADERS := *.h $(LIBMISC_SRC)/*.h
OBJS = $(patsubst %.cpp,%.o,$(patsubst %.cc,%.o,$(SOURCES)))

SVN_REV := $(if $(shell which svnversion 1> /dev/null 2>&1 && echo `svnversion -n .` | grep -q "^[0-9]" && echo 1),$(shell svnversion -n . | tee svn_version),$(shell cat svn_version 2> /dev/null))


MARCH := $(shell ${CXX} ${CXXFLAGS} -x c -S -\#\#\# - < /dev/null 2>&1 | grep -m 1 -o -e "march=[^'\"]*" | sed 's,march=,,')

check_march := $(if $(march), , $(error please define an architecture, e.g., 'make march=pentium'))

.PHONY: all clean dist rsync default

default: $(EXES)

$(TARGET) : $(EXE)
$(EXE) : aco.o ant.o beam_element.o $(OBJS)
	${CXX} ${CXXFLAGS} $^ -o $@

gvns_tsptw: gvns.o $(OBJS)
	${CXX} ${CXXFLAGS} $^ -o $@

firstimprov_tsptw: firstimprov.o $(OBJS)
	${CXX} ${CXXFLAGS} $^ -o $@

localsearch_tsptw: localsearch.o $(OBJS)
	${CXX} ${CXXFLAGS} $^ -o $@

all: clean $(EXES)

clean:
	@rm -f *~ *.o core $(EXES)

localsearch.o : $(HEADERS)
firstimprov.o : $(HEADERS)
gvns.o : $(HEADERS)
aco.o ant.o beam_element.o : ant.h beam_element.h $(HEADERS)
$(OBJS): $(HEADERS)


DIST_SRC_FILES =  README Makefile svn_version *.cpp *.cc *.h
DIST_SRC := ${TARGET}-$(VERSION)-src


dist : DEBUG=0
dist : CDEBUG= -DNDEBUG
dist : all
	@(mkdir -p $(BASE_DIR)/$(DIST_SRC)/beam-aco/trunk \
	&& rsync -rlpC --exclude=.svn --exclude=todo --exclude=TODO \
	$(DIST_SRC_FILES) $(BASE_DIR)/$(DIST_SRC)/beam-aco/trunk \
	&& cd $(BASE_DIR) \
	&& rsync -rlLpC --exclude=todo --exclude=TODO --exclude=.svn --exclude='svn-commit.tmp' --relative \
	libmisc/current ./$(DIST_SRC)/     	 	\
	&& tar cf - $(DIST_SRC) | gzip -f9 > $(DIST_SRC).tar.gz \
	&& rm -rf ./$(DIST_SRC)/ \
	&& echo "$(DIST_SRC).tar.gz created." && cd $(PWD) )

rsync : $(EXES)
ifdef RNODE
	rsync -rlpCIzc -L --copy-unsafe-links --exclude=$(EXE) --exclude=.svn \
	--progress --relative --delete . \
        $(patsubst %, --exclude=%, $(EXES)) \
	$(RNODE):~/src/beam-aco/trunk/
	rsync -rlpCIzc -L --copy-unsafe-links \
	--exclude=.svn \
	--progress --delete $(LIBMISC_SRC) \
	$(RNODE):~/src/libmisc/
else
	@echo "ERROR: You must specify a remote node (e.g., RNODE=b21)"
	@exit 1
endif
