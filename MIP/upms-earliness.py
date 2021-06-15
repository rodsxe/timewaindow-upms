from gurobipy import *
import os
from io import StringIO 
import sys, io
from io import TextIOWrapper, BytesIO


class Capturing(list):
    def __enter__(self):
        self._stdout = sys.stdout
        sys.stdout = self._stringio = StringIO()
        return self
    def __exit__(self, *args):
        self.extend(self._stringio.getvalue().splitlines())
        del self._stringio    # free up some memory
        sys.stdout = self._stdout


#try:

N = None; M = None
tokens = None
p = None
s = None
validate_sol = {'obj_f':None, 'jobs':{}}
is_to_validate = False
path = '/home/rodney/instances/small/'
instances = os.listdir(path)
f_obj_result =  open(path +'../result.txt', "w")

for instance in instances:

    f_result = open(path +'../result/' + instance, "w")
    f = open(path + instance, "r")

    tokens = f.readline().split()
    N = int(tokens[0])
    M = int(tokens[1])

    tokens = f.readline().split()
    tokens = f.readline().split()
    tokens = f.readline().split()

    p = []
    for j in range(M):
        p.append([])

    for j in range(N):
        tokens = f.readline().split()
        i = 0
        for k in range(len(tokens)):
            p[i].append(int(tokens[k]))
            i = i + 1
                
    print(p)

    tokens = f.readline().split()
    tokens = f.readline().split()
    tokens = f.readline().split()

    s = []

    for i in range(M):
        tokens = f.readline().split()
        s.append([])
        for j in range(N):
            s[i].append([])
            tokens = f.readline().split()
            for k in range(len(tokens)):
                s[i][j].append(int(tokens[k]))
        
    print(s)

    tokens = f.readline().split()
    tokens = f.readline().split()

    e_date = []
    d_date = []

    tokens = f.readline().split()
    for k in range(len(tokens)):
        d_date.append(int(tokens[k]))      

    tokens = f.readline().split()
    tokens = f.readline().split()
    for k in range(len(tokens)):
        e_date.append(int(tokens[k]))      


    print("Due dates:")
    print(e_date)
    print(d_date)

    t_weight = []
    tokens = f.readline().split()
    tokens = f.readline().split()
    for k in range(len(tokens)):
        t_weight.append(int(tokens[k]))      

    print("Tardiness weights:")
    print(t_weight)


    e_weight=[]
    tokens = f.readline().split()
    tokens = f.readline().split()
    for k in range(len(tokens)):
        e_weight.append(int(tokens[k]))      

    print("Earliness weights:")
    print(e_weight)

    f.close() 

    if is_to_validate:
        
        f_sol = open(path + '../validate_sols/' + instance, "r")
        tokens = f_sol.readline().split()
        validate_sol['obj_f'] = float(tokens[3])
        for m in range(M):
            previous = None
            tokens = f_sol.readline().split()
            for n in range(2, len(tokens)):
                validate_sol['jobs'][tokens[n]] = { 'previous': previous if previous is not None else 0,
                                                    'job': int(tokens[n]),
                                                    'm': m,
                                                    'C_j':None,
                                                    'E_j':None,
                                                    'T_j':None,
                                                    }
                previous = int(tokens[n])

        tokens = f_sol.readline().split()

        for n in range(N):
        
            tokens = f_sol.readline().split()
            validate_sol['jobs'][tokens[0]]['C_j'] = int(tokens[1])
            validate_sol['jobs'][tokens[0]]['T_j'] = int(tokens[3])
            validate_sol['jobs'][tokens[0]]['E_j'] = int(tokens[2])

        f_sol.close()    
        

    try:
        # Create a new model
        m = Model("UPMS")

        #Create variables
        X = []
        for i in range(M):
            Xj = []
            for j in range(N+1):
                Xk = [m.addVar(vtype=GRB.BINARY, name="X"+str(i) + '_' + str(j) + '_' + str(k)) for k in range(N)]
                Xj.append(Xk)
            X.append(Xj)

        # Create variables job completion time
        C = []
        for i in range(M):
            C.append([m.addVar(name="C"+str(i) + '_' + str(j)) for j in range(N + 1)])
        
        E = []
        T = []
        for i in range(N):
            T.append(m.addVar(name="T_"+str(i)))
            E.append(m.addVar(name="E_"+str(i)))
        
        # Create variables Cmax
        TWET = m.addVar(name="TWET")
        
        # Set objective
        m.setObjective(TWET, GRB.MINIMIZE)

        # Add constraints: Each Job assigned in only one machine and have one predecessor.
        [m.addConstr(sum([sum([X[i][j][k] if j - 1 != k else 0 for j in range(N + 1)]) for i in range(M)]) == 1, "2_"+str(k+1)) for k in range(N)]
        # Add constraints: set the maximum number of successors of every job to one
        [m.addConstr(sum([sum([X[i][j+1][k] if j != k else 0 for k in range(N)]) for i in range(M)]) <= 1, "3_"+str(j+1)) for j in range(N)]
        # Add constraints: Each machine have in maximum one job successor the initial state
        [m.addConstr(sum([X[i][0][k] for k in range(N)])  <= 1, "4_"+str(i+1)) for i in range(M)]

        # Add constraints: ensure that jobs are properly order in machine
        for i in range(M):
            for j in range(N):
                for k in range(N):
                    if j != k:
                        m.addConstr(sum([X[i][h][j] if h != j+1 and h != k+1 else 0 for h in range(N + 1)])  >= X[i][j+1][k] , "5_" + str(i+1) + '_' + str(j+1) + '_' + str(k+1))
        
        # Add constraints: ensure that completition time of a successor job k must be small than the predecessor job.  
        for i in range(M):
            for j in range(N + 1):
                for k in range(N):
                    if j != k + 1:
                        m.addConstr(C[i][k+1] + 100000*(1 - X[i][j][k]) >= C[i][j] + (s[i][j - 1][k] if j > 0 else s[i][k][k]) + p[i][k] , '6_' + str(i+1)+'_'+str(j) + '_' + str(k+1))


        # Add constraints: completition time of the initial state job must be 0 
        [m.addConstr(C[i][0]  == 0, "7_"+str(i+1)) for i in range(M)]

        # Add constraints: completition time of the jobs must greather or equal to 0 
        for i in range(M):
            [m.addConstr(C[i][j+1]  >= 0, "8_"+str(i+1)+'_'+str(j+1)) for j in range(N)]
        
        # Add constraints: definition of earlines and tardiness
        for j in range(N):
            [m.addConstr(E[j] >= e_date[j] - C[i][j+1], "9_"+str(i+1)+'_'+str(j+1)) for i in range(M)]
            [m.addConstr(T[j] >=  C[i][j+1] - d_date[j], "10_"+str(i+1)+'_'+str(j+1)) for i in range(M)]
            m.addConstr(T[j] >= 0, "11_" + str(j+1))
            m.addConstr(E[j] >= 0, "12_" + str(j+1))

        for i in range(M):
            [m.addConstr(X[i][j + 1][j] == 0, "13"+str(i+1)+str(j+1)) for j in range(N)]
        
        # Add constraints: definition of objective function
        m.addConstr(TWET == sum([(E[j] * e_weight[j]) + (T[j] * t_weight[j]) for j in range(N)]), "14")
        

        m.write('upms.lp')
        #m.setParam('TimeLimit', 3 * 60 * 60)
        if is_to_validate:
            
            jobs = validate_sol['jobs'].keys()
            for i in range(M):
                 C[i][0].Start = 0

            for j in jobs:
                job = validate_sol['jobs'][j]
                for i in range(M):
                    if i == job['m']: C[job['m']][job['job']].Start = job['C_j']
                    else: C[i][job['job']].Start = e_date[job['job']-1]
                
                E[job['job'] - 1].Start = job['E_j']
                T[job['job'] - 1].Start = job['T_j']
                
                for i in range(M):
                    for j in range(N + 1):
                        if j == job['previous'] and i == job['m']:
                            X[i][j][job['job'] - 1].Start = 1
                        else: 
                            X[i][j][job['job'] - 1].Start = 0 
            
            m.setParam('TimeLimit', 1)
            os.remove('validate_constraint.log')
            m.setParam('LogFile', 'validate_constraint.log')
            
            m.optimize()
            f_validate_constraints = open('validate_constraint.log')
            
            if 'start violates constraint' in f_validate_constraints.read().replace('\n', ''):
                raise Exception("Solution violates constraint")    
            
            if 'MIP start did not produce a new incumbent solution' in f_validate_constraints.read().replace('\n', ''):
                raise Exception("MIP start did not produce a new incumbent solution")    
            
            if m.objVal != validate_sol['obj_f']:
                raise Exception("Upperbound differs than found by heuristic.")    
            
            f_validate_constraints.close()

        else:                                 
            m.setParam('TimeLimit', 3 * 60 * 60)
             
            m.optimize()

            for v in m.getVars():
                f_result.write('%s %g' % (v.varName, v.x))

            f_result.write('\n Obj: %g' % m.objVal)
            f_result.write('\n Time: %g' % m.Runtime)
            f_result.close()

            f_obj_result.write('\n {0};{1};{2}'.format(instance,m.objVal, m.Runtime))
            f_obj_result.flush()

    except GurobiError as e:
        print('Error code ' + str(e.errno) + ": " + str(e))

    except AttributeError:
        print('Encountered an attribute error')

f_obj_result.close() 