#---------------------------------------------------------------------
#
# Makefile for libmisc
#
VERSION := 0.05
#
# $Name$ $Revision$  $Date$
#
#---------------------------------------------------------------------
#
# Copyright (c) 2005-2013 Manuel Lopez-Ibanez
# TeX: \copyright 2005-2013 Manuel L{\'o}pez-Ib{\'a}{\~n}ez
#
# This program is free software (software libre); you can redistribute
# it and/or modify it under the terms of the GNU General Public
# License version 2 as published by the Free Software Foundation.
#
# This program is distributed in the hope that it will be useful, but
# WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
# General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program; if not, you can obtain a copy of the GNU
# General Public License at:
#                http://www.gnu.org/copyleft/gpl.html
# or by writing to:
#          Free Software Foundation, Inc., 59 Temple Place,
#                Suite 330, Boston, MA 02111-1307 USA
#
#---------------------------------------------------------------------

DEBUG=1 # =2 extra information is generated and checks are perfomed
        # =1 only checks are performed
        # =0 disable (fastest).

ECHO = @echo "$(1)"

uname_S := $(shell gcc -dumpmachine 2>&1)
uname_S := $(subst MINGW,mingw,$(uname_S))
ifneq (,$(findstring mingw,$(uname_S)))
RM=-del /Q
CC=gcc
uname_S:=mingw
else
## Do we have svnversion?
ifeq ($(shell sh -c 'which svnversion 1> /dev/null 2>&1 && echo y'),y)
    ## Is this a working copy?
    ifeq ($(shell sh -c 'LC_ALL=C svnversion -n . | grep -q ^[0-9] && echo y'),y)
      $(shell sh -c 'svnversion -n . > svn_version')
    endif
endif
## Set version information:
SVN_REV = $(shell sh -c 'cat svn_version 2> /dev/null')
endif

TEST_EXES = timer-test

OFLAGS = -O3
EXTRAWARNINGS := -Wall -W -Wfloat-equal
# FIXME: -O3 -finline is needed because gsl_rng_uniform has to be
# inline. How to avoid "undefined reference to `gsl_rng_uniform'" when
# using "-O0 -fkeep-inline-functions"?
override CFLAGS := -std=gnu99 $(OFLAGS)  $(EXTRAWARNINGS) -D DEBUG=$(DEBUG) -O3 -finline

ifneq ($(DEBUG), 0)
override CFLAGS += -g3 -DMALLOC_CHECK_=3
endif

include libmisc.mk

.PHONY : all cleanall cleanexe cleantest clean library test

# Create static library.
library : $($(lib)_LIB)

%.o : %.c
	$(CC) $(CFLAGS) -c -o $@ $<

%-test : %-test.c $($(lib)_LIB)
	$(CC) $(CFLAGS) -o $@ $< $(LDFLAGS) -lm

test : $(TEST_EXES)

cleantest:
	@$(RM) $($(lib)_SRCDIR)/*-test

cleanall : clean cleanexe cleantest

cleanexe :
	$(call ECHO,---> Removing $($(lib)_LIB) <---)
	$(RM) $($(lib)_LIB)

clean : LIB_OPTS := clean
clean :
	$(call ECHO,---> Removing object files <---)
	@$(RM) $($(lib)_OBJS)
	@$(RM) $($(lib)_SRCDIR)/*~

all :  LIB_OPTS := all
all :  cleanall library

dist : DEBUG=0
dist : CDEBUG=
dist : DIST_SRC := libmisc-$(VERSION)-src
dist : all
	@(mkdir -p ../$(DIST_SRC) && rsync -aC . ../$(DIST_SRC)/ && cd .. \
	&& tar cf - $(DIST_SRC) | gzip -f9 > $(DIST_SRC).tar.gz \
	&& rm -rf ./$(DIST_SRC)/ \
	&& echo "$(DIST_SRC).tar.gz created." && cd $(PWD) )
