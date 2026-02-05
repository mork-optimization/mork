#ifndef GCC_ATTRIBUTES
#define GCC_ATTRIBUTES

/* FIXME: does this handle C++? */
/* FIXME: add the explanation from the GCC documentation to each attribute. */
#if __GNUC__ >= 3
# define inline		inline /* __attribute__ ((always_inline)) */
/* always_inline produces errors like:
   sorry, unimplemented: inlining failed
*/
# define __pure_func	__attribute__ ((pure))
# define __const_func	__attribute__ ((const))
# define __noreturn	__attribute__ ((noreturn))
# define __malloc	__attribute__ ((malloc))
# define __must_check	__attribute__ ((warn_unused_result))
# define __deprecated	__attribute__ ((deprecated))
# define __used		__attribute__ ((used))
# define __unused	__attribute__ ((unused))
# define __packed	__attribute__ ((packed))
# define likely(x)	__builtin_expect (!!(x), 1)
# define unlikely(x)	__builtin_expect (!!(x), 0)
#else
# warning "__GNUC__ < 3: disabling GCC attributes"
# define inline		/* no inline */
# define  __attribute__(x) /* If we're not using GNU C, elide __attribute__ */
# define likely(x)	(x)
# define unlikely(x)	(x)
#endif

#endif
