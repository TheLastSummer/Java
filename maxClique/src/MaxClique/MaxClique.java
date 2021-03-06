package MaxClique;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class MaxClique implements Runnable{
    int size;
    int[][] map;
    int[][] alt;
    int answer;
    int[] max;
    boolean flag;
    public static HashMap<Integer, Integer> hashMap = new HashMap<>();

    public MaxClique(Set set) {
        this.size = set.size();
        alt = new int[size][size];
        this.map = changeForm(set);
        max = new int[size];
        answer = 0;
    }

    public boolean dfs(int current, int total){
        if(current == 0){
            if(total > answer){
                answer = total;
                return true;
            }
            return false;
        }
        for(int i = 0; i < current; i++){
            if(current - i + total <= answer)
                return false;
            int u = alt[total][i];
            if(max[u] + total <= answer)
                return false;
            int nxt = 0;
            for(int j = i + 1; j < current; j++){
                if(map[u][alt[total][j]] == 1){
                    alt[total + 1][nxt++] = alt[total][j];
                }
            }
            if(dfs(nxt, total + 1))
                return true;
        }
        return false;
    }

    public int maxClique(){
        for(int i = size - 1; i >= 0; i--){
            int current = 0;
            for(int j = i + 1; j < size; j++)
                if(map[i][j] == 1)
                    alt[1][current++] = j;
            dfs(current, 1);
            max[i] = answer;
        }
        flag = true;
        return answer;
    }

    public int[][] changeForm(Set set){
        ArrayList<Integer> input = new ArrayList<>();
        input.addAll(set.getAllElements());
        int[][] points = new int[input.size()][input.size()];
        for(int i = 0; i < input.size(); i++){
            hashMap.put(input.get(i), i);
        }
        Iterator<Integer> iterator = input.iterator();
        while(iterator.hasNext()){
            int father = iterator.next();
            Set neighbours = InputHandler.PointArray[father].Neighbour;
            for(int i =0; i <= InputHandler.PointsNum;i++){
                if(neighbours.contains(i) && set.contains(i)){
                    points[hashMap.get(father)][hashMap.get(i)] = 1;
                    points[hashMap.get(i)][hashMap.get(father)] = 1;
                }
            }
        }
        return points;
    }

    @Override
    public void run(){
        maxClique();
    }

}
