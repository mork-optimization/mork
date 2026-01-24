#ifndef XVECTOR_HPP
#define XVECTOR_HPP
#include <vector>
// FIXME: What is the most efficient way to move an element
// to a different position in the same vector?
// Option 1: std::rotate seems to be slower than erase + insert!
// template<typename T>
// static void
// reinsert(std::vector<T> & v, size_t from, size_t to)
// {
//     typename std::vector<T>::iterator first, middle, last;
//     if (from < to) {
//         first  = v.begin() + from;
//         middle = first + 1;
//         last   = v.begin() + to;
//     } else {
//         first  = v.begin() + to;
//         middle = v.begin() + from;
//         last   = middle + 1;
//     }
//     std::rotate(first, middle, last);
// }
// FIXME: For sure we do not need to pass the element.
template<class T>
static void reinsert(std::vector<T> &v, const T &element, int from, int to)
{
    v.erase(v.begin() + from);
    v.insert(v.begin() + to, element);
}

template<class T>
static void reinsert(std::vector<T> &v, int from, int to)
{
    const T element = v[from];
    v.erase(v.begin() + from);
    v.insert(v.begin() + to, element);
}

#include <cmath> // sqrt

template<typename T>
static T euclid_distance(const std::vector<T> &a,
                         const std::vector<T> &b)
{
    T sum = 0;
    size_t size = a.size();
    for (size_t i = 0; i < size; i++) {
        T x = a[i] - b[i];
        sum += x * x;
    }
    return sqrt(sum);
}

#include <algorithm>    // std::min_element, std::max_element

/* FIXME: How to avoid the unused vector just to find the type of the vector? */
template<class T, class Iter>
static bool is_a_permutation(const std::vector<T> &unused, Iter first,
                             Iter last)
{
    std::vector<T> v (first, last);
    std::sort(v.begin(), v.end());
    Iter it = std::adjacent_find(v.begin(), v.end());
    return (it == v.end());
}

template<typename T>
static T max_of(const std::vector<T> &x)
{
    return *max_element (x.begin(), x.end());
}

#endif
