#include "beam_element.h"

void Beam_Element::commit()
{
  int last = permutation.back();
  add (last, node);
  DEBUG3(fprintf(stderr, "commit: "); print_one_line(stderr));
}

Beam::iterator
Beam::random_wheel (Random * rnd, double basesum)
{
  double rand = rnd->next() * basesum;
  Beam::iterator it = this->begin();
  double wheel = (*it)->value;

  while (wheel < rand) {
    it++;
    wheel +=  (*it)->value;
    //    cerr << (*it)->node << "  " << (*it)->value << endl;
  }
  assert ((*it)->node >= 0);
  assert (it != this->end());

  return it;
}

bool child_greedy_weight_compare(const Beam_Element * c1, 
                                 const Beam_Element * c2)
{
  return c1->greedy_weight > c2->greedy_weight;
}

bool solution_cost_compare (const Beam_Element* c1, const Beam_Element* c2)
{
  return c1->better_than (c2);
}

Beam
Beam_Element::produce_children (int max_children) 
{
  Beam child_list;

  // Create new partial solutions by adding unassigned nodes to the
  // current solution.
  for (int k = 0, inode = 0; k < nodes_available; k++) {
    while (node_assigned[++inode]);

    // Creates a child by copying solution.
    Beam_Element * child = this->clone();
    child->node = inode;
    int last = child->permutation.back();
    child->greedy_weight = child->heuristic_information (last, child->node);

    child_list.push_back (child);
    DEBUG3 (fprintf (stderr, "Child: %2d, %g\t", 
                     child->node, child->greedy_weight);
            child->print_one_line(stderr));
  }

  child_list.sort (child_greedy_weight_compare);

  int count = 1;
  Beam::iterator chi = child_list.begin();
  while (count <= max_children and chi != child_list.end()) {
    Beam_Element *child = *chi;
    
    child->greedy_rank_sum = child->greedy_rank_sum + count;

    int last = child->permutation.back();
    child->value = child->pheromone[last][child->node];

    chi++;
    count++;
  }

  // Cleanup unused children
  Beam::iterator chi2 = chi;
  while (chi2 != child_list.end()) {
    Beam_Element *child = *chi2;
    delete child;
    chi2++;
  }
  child_list.erase (chi, child_list.end());
  
  return child_list;
}

bool child_value_compare(const Beam_Element * c1, const Beam_Element * c2)
{
  /*  return (c1->value > c2->value) ? true 
    : (c1->value < c2->value) ? false
    : (Ant::rng->next() < 0.5) ? true : false;*/
  return c1->value > c2->value;
}

/* Create a new beam from children with at most to_choose elements.  */
void
Beam::choose_from (Beam &children, double children_basesum, 
                   int to_choose, double det_rate, Random *rng)
{
  if (int(children.size()) <= to_choose) {

    DEBUG3 (fprintf (stderr, 
                     "Children.size (%d) <= to_choose (%d) (basesum = %g)\n", 
                     children.size(), to_choose, children_basesum);
            int k = 0;
            for (Beam::iterator child = children.begin(); 
                 child != children.end(); child++) {
              fprintf(stderr, "Child %2d: node = %2d  val = %.7f  "
                      "g_w = %.7f  g_r_s = %4g  ",
                      k++, (*child)->node, (*child)->value, 
                      (*child)->greedy_weight, 
                      (*child)->greedy_rank_sum);
              (*child)->print_one_line (stderr);
            });

    for (Beam::iterator child = children.begin(); 
         child != children.end(); child++) {
      (*child)->commit();
      this->push_back (*child);
    }
  }
  else {
    children.sort (child_value_compare);
    
    DEBUG3 (fprintf (stderr, 
                     "Children.size (%d) > to_choose (%d) (basesum = %g)\n", 
                     children.size(), to_choose, children_basesum);
            int k = 0;
            for (Beam::iterator child = children.begin(); 
                 child != children.end(); child++) {
              fprintf(stderr, "Child %2d: node = %2d  val = %.7f  "
                      "g_w = %.7f  g_r_s = %4g  ",
                      k++, (*child)->node, (*child)->value, 
                      (*child)->greedy_weight, 
                      (*child)->greedy_rank_sum);
              (*child)->print_one_line (stderr);
            });
    
    for (int i = 0; i < to_choose; i++) {
      list <Beam_Element *>::iterator child;
      
      if (det_rate >= 1.0 or (det_rate > 0.0  and rng->next() < det_rate)) {
        
        child = children.begin();
        DEBUG3 (fprintf(stderr, "choose determ: "
                        "node = %2d  val = %.7f g_w = %.7f  g_r_s = %4g  ",
                        (*child)->node, (*child)->value, 
                        (*child)->greedy_weight, (*child)->greedy_rank_sum));
      } else {
        
        child = children.random_wheel (rng, children_basesum);
        DEBUG3 (fprintf(stderr,"choose random: " 
                        "node = %2d  val = %.7f g_w = %.7f  g_r_s = %4g  ",
                        (*child)->node, (*child)->value, 
                        (*child)->greedy_weight, (*child)->greedy_rank_sum));
      }
      (*child)->commit();
      this->push_back (*child);
      children_basesum = children_basesum - (*child)->value;
      children.erase (child);
    }
    // Delete all unused children (those never stored in the new_beam). 
    for (Beam::iterator child = children.begin(); 
         child != children.end(); child++) {
      delete (*child);
    }
  }
  children.clear();
}

