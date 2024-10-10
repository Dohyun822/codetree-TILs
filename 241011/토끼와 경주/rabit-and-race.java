import java.util.*;
import java.io.*;

public class Main {
    private static int Q;
    private static int[][] map;
    private static ArrayList<Main.Rabbit> list;

    private static class Rabbit {
        int id;
        int dist;
        int count;
        int score;
        int r;
        int c;

        public Rabbit(int id, int dist, int count, int score, int r, int c) {
            this.id = id;
            this.dist = dist;
            this.count = count;
            this.score = score;
            this.r = r;
            this.c = c;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getDist() {
            return dist;
        }

        public void setDist(int dist) {
            this.dist = dist;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public int getScore() {
            return score;
        }

        public void setScore(int score) {
            this.score = score;
        }

        public int getR() {
            return r;
        }

        public void setR(int r) {
            this.r = r;
        }

        public int getC() {
            return c;
        }

        public void setC(int c) {
            this.c = c;
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Rabbit other = (Rabbit) obj;
            return id == other.id;
        }
    }

    private static int[] dr = { -1, 0, 1, 0 };
    private static int[] dc = { 0, 1, 0, -1 }; // 상우하좌
    private static ArrayList<Integer> chkList;

    public static void main(String[] args) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        Q = Integer.parseInt(br.readLine());

        for (int i = 0; i < Q; i++) {
            StringTokenizer st = new StringTokenizer(br.readLine(), " ");

            int command = Integer.parseInt(st.nextToken());

            switch (command) {
            case 100: // 경주 시작 준비
                int N = Integer.parseInt(st.nextToken());
                int M = Integer.parseInt(st.nextToken());
                map = new int[N + 1][M + 1];
                int P = Integer.parseInt(st.nextToken());

                list = new ArrayList<Rabbit>();

                for (int j = 0; j < P; j++) {
                    int pid = Integer.parseInt(st.nextToken());
                    int dist = Integer.parseInt(st.nextToken());

                    list.add(new Rabbit(pid, dist, 0, 0, 1, 1));
                }
                break;
            case 200: // 경주 진행
                chkList = new ArrayList<Integer>();
                int K = Integer.parseInt(st.nextToken()); // K번의 턴 동안
                for (int j = 0; j < K; j++) {
                    Rabbit selected = findRabbit();
                    for (Rabbit rab : list) {
                        if (rab.getId() == selected.getId()) {
                            rab.setCount(rab.getCount() + 1);
                        }
                    }
                    chkList.add(selected.getId());
                    move(selected);
                }
                int S = Integer.parseInt(st.nextToken()); // 우선순위 가장 높은 토끼에게 점수 S를 더해줌
                Rabbit selected = find_sRabbit();
                for (Rabbit rab : list) {
                    if (rab.getId() == selected.getId()) {
                        rab.setScore(rab.getScore() + S);
                    }
                }
                break;
            case 300: // 이동거리 변경
                int pid = Integer.parseInt(st.nextToken()); // 고유번호
                int L = Integer.parseInt(st.nextToken()); // 이동거리 L 배
                for (Rabbit rab : list) {
                    if (rab.getId() == pid) {
                        long newDist = (long)rab.getDist() * L;
                        rab.setDist((int)Math.min(newDist, 1_000_000_000));
                    }
                }
                break;
            case 400: // 최고의 토끼 선정
                int score = 0;
                for (Rabbit rab : list) {
                    if (rab.getScore() > score) {
                        score = rab.getScore();
                    }
                }
                System.out.println(score);
                break;
            }
        }
    }

    private static Main.Rabbit find_sRabbit() {
        Rabbit temp = new Rabbit(0, 0, 0, 0, 0, 0);
        for (Rabbit cur : list) {
            if (chkList.contains(cur.getId())) {
                if (cur.getR() + cur.getC() > temp.getR() + temp.getC() ||
                    (cur.getR() + cur.getC() == temp.getR() + temp.getC() && cur.getR() > temp.getR()) ||
                    (cur.getR() + cur.getC() == temp.getR() + temp.getC() && cur.getR() == temp.getR() && cur.getC() > temp.getC()) ||
                    (cur.getR() + cur.getC() == temp.getR() + temp.getC() && cur.getR() == temp.getR() && cur.getC() == temp.getC() && cur.getId() > temp.getId())) {
                    temp = cur;
                }
            }
        }
        return temp;
    }

    private static void move(Main.Rabbit selected) {
        int d = selected.getDist(); // 이동 해야 하는 거리
        int r = selected.getR();
        int c = selected.getC();

        // 상, 우, 하, 좌로 각각 이동한 위치 저장
        int[][] positions = new int[4][2]; // {r, c}

        for (int i = 0; i < 4; i++) { // 상우하좌
            int nr = r;
            int nc = c;

            if (i == 0 || i == 2) { // 상 or 하 방향 이동
                int N = map.length - 1; // 행의 최대값
                nr = r + (i == 0 ? d : -d);
                nr = ((nr - 1 + 2*(N-1)) % (2*(N-1))) + 1;
                if (nr > N) nr = 2*N - nr;
            } else { // 좌 or 우 방향 이동
                int M = map[0].length - 1; // 열의 최대값
                nc = c + (i == 1 ? d : -d);
                nc = ((nc - 1 + 2*(M-1)) % (2*(M-1))) + 1;
                if (nc > M) nc = 2*M - nc;
            }

            positions[i][0] = nr;
            positions[i][1] = nc;
        }

        // 우선순위에 따라 위치 선택
        int[] bestPos = positions[0];
        for (int i = 1; i < 4; i++) {
            int curSum = positions[i][0] + positions[i][1];
            int bestSum = bestPos[0] + bestPos[1];

            if (curSum > bestSum ||
                (curSum == bestSum && positions[i][0] > bestPos[0]) ||
                (curSum == bestSum && positions[i][0] == bestPos[0] && positions[i][1] > bestPos[1])) {
                bestPos = positions[i];
            }
        }

        // 토끼의 위치를 선택된 위치로 업데이트, 나머지 토끼들 점수 추가
        for (Rabbit rab : list) {
            if (rab.getId() == selected.getId()) {
                rab.setR(bestPos[0]);
                rab.setC(bestPos[1]);
            } else {
                rab.setScore(rab.getScore() + bestPos[0] + bestPos[1]);
            }
        }
    }

    private static Main.Rabbit findRabbit() {
        Rabbit temp = new Rabbit(0, 0, Integer.MAX_VALUE, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);
        for (Rabbit cur : list) {
            if (cur.getCount() < temp.getCount() ||
                (cur.getCount() == temp.getCount() && cur.getR() + cur.getC() < temp.getR() + temp.getC()) ||
                (cur.getCount() == temp.getCount() && cur.getR() + cur.getC() == temp.getR() + temp.getC() && cur.getR() < temp.getR()) ||
                (cur.getCount() == temp.getCount() && cur.getR() + cur.getC() == temp.getR() + temp.getC() && cur.getR() == temp.getR() && cur.getC() < temp.getC()) ||
                (cur.getCount() == temp.getCount() && cur.getR() + cur.getC() == temp.getR() + temp.getC() && cur.getR() == temp.getR() && cur.getC() == temp.getC() && cur.getId() < temp.getId())) {
                temp = cur;
            }
        }
        return temp;
    }
}