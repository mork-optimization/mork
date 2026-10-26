// Evaluate imported tours through the actual GVNS Solution implementation.
#include "tsptw_solution.h"
#include <sstream>
#include <iomanip>

int main(int argc, char **argv) {
    if (argc != 3) return 2;
    Solution::LoadInstance(argv[1]);
    std::ifstream input(argv[2]);
    if (!input) return 3;
    std::string line;
    while (std::getline(input, line)) {
        std::istringstream row(line);
        std::string id;
        row >> id;
        std::vector<int> tour;
        int node;
        while (row >> node) tour.push_back(node);
        const int n = Solution::n;
        bool valid = int(tour.size()) == n + 1 && tour.front() == 0 && tour.back() == 0;
        std::vector<bool> seen(n, false);
        if (valid) {
            for (int i = 0; i < n; i++) {
                node = tour[i];
                if (node < 0 || node >= n || seen[node]) { valid = false; break; }
                seen[node] = true;
            }
        }
        std::cout << "{\"id\":\"" << id << "\",\"validPermutation\":" << (valid ? "true" : "false");
        if (valid) {
            Solution solution;
            solution.add(tour.data() + 1);
            bool consistent = solution.check_solution();
            std::cout << ",\"feasible\":" << (consistent && solution.constraint_violations() == 0 ? "true" : "false")
                      << ",\"cost\":" << solution.cost()
                      << ",\"violations\":" << solution.constraint_violations()
                      << ",\"infeasibility\":" << solution.infeasibility()
                      << ",\"makespan\":" << solution.makespan()
                      << ",\"cacheConsistent\":" << (consistent ? "true" : "false");
        } else {
            std::cout << ",\"feasible\":false";
        }
        std::cout << "}" << std::endl;
    }
    return 0;
}
