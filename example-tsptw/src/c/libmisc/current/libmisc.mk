#
lib=LIBMISC
$(lib)_SRCDIR ?= .

$(lib)_OBJS = myrandom.o parameter.o timer.o mymalloc.o work.o eprintf.o meminfo.o rng.o taus.o

$(lib)_HDRS = myrandom.h parameter.h timer.h mymalloc.h work.h common.h eprintf.h libmisc.h fatal.h gcc_attribs.h misc-math.h t_number.h meminfo.h debug.h xvector.hpp

ifeq ($(uname_S),mingw)
	$(lib)_OBJS += getrusage.c
	$(lib)_HDRS += resource.h
endif

$(lib)_OBJS :=$(foreach OBJ, $($(lib)_OBJS), $(join $($(lib)_SRCDIR)/, $(OBJ)) )
$(lib)_HDRS :=$(foreach OBJ, $($(lib)_HDRS), $(join $($(lib)_SRCDIR)/, $(OBJ)) )

$(lib)_INCLUDEDIR = $($(lib)_SRCDIR)/../
$(lib)_LIB=$($(lib)_INCLUDEDIR)/libmisc.a

override CFLAGS += -I$($(lib)_INCLUDEDIR)
override LDFLAGS += $($(lib)_INCLUDEDIR)/libmisc.a

$($(lib)_LIB): $($(lib)_OBJS)
	@echo "--> Creating static library \"$($(lib)_LIB)\" version $(VERSION) <---"
	$(QUIET_AR)$(RM) $@ && $(AR) rcs $@ $^
	cp -f $($(lib)_HDRS) $($(lib)_INCLUDEDIR)/
	chmod -w $($(lib)_INCLUDEDIR)/*.h??

## Dependencies:
$($(lib)_OBJS): $($(lib)_HDRS)


