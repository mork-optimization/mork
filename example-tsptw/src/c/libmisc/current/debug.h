#ifndef _DEBUG_H_

#ifndef DEBUG
#define DEBUG 0
#endif

#if DEBUG >= 1
#define DEBUG1(X) X;(void)0
#else  
#define DEBUG1(X) (void)0
#endif

#if DEBUG >= 2
#define DEBUG2(X) X;(void)0
#else  
#define DEBUG2(X) (void)0
#endif

#if DEBUG >= 3
#define DEBUG3(X) X;(void)0
#else  
#define DEBUG3(X) (void)0
#endif

#if DEBUG >= 4
#define DEBUG4(X) X;(void)0
#else  
#define DEBUG4(X) (void)0
#endif

#define DEBUG1_PRINT(...) DEBUG1 (fprintf (stderr, __VA_ARGS__))

#define DEBUG1_FUNPRINT(...) \
    do { DEBUG1_PRINT ("%s(): ", __FUNCTION__); \
         DEBUG1_PRINT (__VA_ARGS__); } while(0)

#define DEBUG2_PRINT(...) DEBUG2 (fprintf (stderr, __VA_ARGS__))

#define DEBUG2_FUNPRINT(...) \
    do { DEBUG2_PRINT ("%s(): ", __FUNCTION__); \
         DEBUG2_PRINT (__VA_ARGS__); } while(0)

#define DEBUG3_PRINT(...) DEBUG3 (fprintf (stderr, __VA_ARGS__))

#define DEBUG3_FUNPRINT(...) \
    do { DEBUG3_PRINT ("%s(): ", __FUNCTION__); \
         DEBUG3_PRINT (__VA_ARGS__); } while(0)

#if DEBUG >= 1
#ifndef MALLOC_CHECK_
#define MALLOC_CHECK_ 3
#endif
#endif

#endif 
