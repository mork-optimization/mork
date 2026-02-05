#include <stdio.h>
#include <stdlib.h>
#include <stdarg.h>
#include "eprintf.h"
/* From:
 	The GNU C Library Reference Manual

        	Sandra Loosemore
    	    	     with
        Richard M.Stallman, Roland McGrath,
        Andrew Oram and Ulrich Drepper

        Edition 0.07 DRAFT 4 Oct 1996 for version 2.00 Beta

 Copyright (c) 1993, '94, '95, '96, '97 Free Software Foundation, Inc.
 Published by the Free Software Foundation
 59 Temple Place -- Suite 330,
 Boston, MA 02111-1307 USA
 Printed copies are available for $50 each.
 ISBN 1-882114-53-1
*/
void eprintf(const char *template,...)
{
    va_list ap;

    fprintf(stderr, "%s: ", program_invocation_short_name);
    va_start(ap,template);
    vfprintf(stderr, template, ap);
    va_end(ap);

    exit(1);
}
// End of copyright The GNU C Library Reference Manual
