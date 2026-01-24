#ifndef   	LIBMISC_COMMON_H_
#define   	LIBMISC_COMMON_H_

#include <inttypes.h>

#include "debug.h"

#ifndef __cplusplus
#include <stdbool.h>
#ifndef TRUE
#define TRUE  true
#define FALSE false
#endif
#endif

/* C++ needs to know that types and declarations are C, not C++.  */
#ifndef __BEGIN_DECLS
#ifdef  __cplusplus
# define __BEGIN_DECLS  extern "C" {
# define __END_DECLS    }
#else
# define __BEGIN_DECLS
# define __END_DECLS
#endif
#endif

#include "gcc_attribs.h"

typedef unsigned int uint;
typedef unsigned long ulong;
typedef long long longlong;

#endif 	    /* !LIBMISC_COMMON_H_ */
