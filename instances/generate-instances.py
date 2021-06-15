import random 
import os.path
import os

def write_instance(dir_path, instance_name, N, M, setup_factor, TF, RDD, processing_time, setup_times, due_dates, earliness_dates, tardiness_weights, earliness_weights):

    f = open(dir_path + instance_name, "w")
    f.write(str(N) + " " + str(M) + " " + str(setup_factor) + " " + str(TF) + " " + str(RDD))
    f.write("\n\n")
    f.write("####PROCESSING_TIMES####")
    f.write("\n\n")
    for j in range(N):
        for i in range(M):
            f.write("%d " % processing_time[i][j])
        
        f.write("\n")
    f.write("\n")
    f.write("####SETUP_TIMES####")
    f.write("\n\n")
    
    for i in range(M):
        f.write("M%d\n" % (i + 1))
        for j in range(N):
            for k in range(N):
                f.write("%d " % setup_times[i][j][k])
            f.write("\n")
    f.write("\n")

    f.write("####DUE DATES####")
    f.write("\n")

    for j in range(N):
        f.write("%d " % due_dates[j])
    f.write("\n")
    
    f.write("####EARLINESS DATES####")
    f.write("\n")

    for j in range(N):
        f.write("%d " % earliness_dates[j])
    f.write("\n")
    
    f.write("####TARDINESS WEIGHTS####")
    f.write("\n")

    for j in range(N):
        f.write("%d " % tardiness_weights[j])
    f.write("\n")
    
    f.write("####EARLINESS WEIGHTS####")
    f.write("\n")

    for j in range(N):
        f.write("%d " % earliness_weights[j])
    f.write("\n")
    
    f.close()


def generate_instance(N, M, setup_factor, TF, RDD):

    setup_times = []
    processing_time = []
    due_dates = None
    earliness_dates = None
    tardiness_weights = None
    earliness_weights = None

    total_processing_time = 0
    total_setup_times = 0
    mean_processing_time = None
    TPT = None
    centroids_time_window = None
    time_window_center_inf = None
    time_window_center_sup = None 

    #generate the processing times
    n_groups = int(N/7 + 1)
    speed_factor = [1/random.randrange(5, 16) for i in range(n_groups)]
    job_assigned_group = [random.randrange(0, n_groups) for j in range(N)]
    
    for i in range(M):
        processing_time.append([int(1/speed_factor[job_assigned_group[j]] * random.randrange(10, 40)) for j in range(N)])
        total_processing_time += sum(processing_time[i])
    
    #generate setup_times
    mean_processing_time = total_processing_time/(M*N)
    
    for i in range(M):
        setup_times.append([])
        for j in range(N):
            setup_times[i].append([])
            for k in range(N):
                #setup_times[i][j].append(random.randrange(0, int((setup_factor * mean_processing_time))))
                setup_times[i][j].append(random.randrange(10, 101))
            
            total_setup_times += sum(setup_times[i][j])

    CMAX = 0;
    for k in range(N):
        for i in range(M):
            total_setup_job = 0
            for j in range(N):
                total_setup_job += setup_times[i][j][k]  
            CMAX += (processing_time[i][k] + total_setup_job/N)/M 
    
    CMAX = CMAX/M

    #generate time windows
    TPT = CMAX

    time_window_center_inf = int(abs(1 - TF - RDD/2) * TPT)
    time_window_center_sup = int(abs(1 - TF + RDD/2) * TPT)
    
    centroid_time_window = [random.randrange(time_window_center_inf, time_window_center_sup)  for j in range(N)]

    due_dates = [centroid_time_window[j] + int(random.randrange(0, int(TPT/N)))  for j in range(N)]
    earliness_dates = [max(0,centroid_time_window[j] - (due_dates[j] - centroid_time_window[j])) for j in range(N)]
    
    #generate weights
    tardiness_weights = [random.randrange(0, 10) + 1 for j in range(N)]
    earliness_weights = [random.randrange(0, tardiness_weights[j]) + 1 for j in range(N)]
    
    
    return processing_time, setup_times, due_dates, earliness_dates, tardiness_weights, earliness_weights


def main():

    PATH = "/home/rodney/instances/large/"
    N = [12]
    M = [3]
    SF = [2.00]
    TF = [0.4, 0.8]
    RDD = [0.4, 0.8, 1.2]
    NUMBER_OF_INSTANCES = 20

    for n in N:
        for m in M:
            for sf in SF:
                for tf in TF:
                    for rdd in RDD: 
                        for i in range(NUMBER_OF_INSTANCES):
                            p, s, dd, ed, tw, ew = generate_instance(n, m, sf, tf, rdd)
                            
                            directory = PATH + str(m) + "m-" + str(n) + "n/"
                            if not os.path.exists(directory):
                                os.mkdir(directory)
                            #directory = PATH
                            instance_name = "instance-%d-%d-%.2f-%.2f-%d.dat"%(n, m, tf, rdd, i)
                            write_instance(directory, instance_name, n, m, sf, tf, rdd, p, s, dd, ed, tw, ew)
    

if __name__ == '__main__':
    main()