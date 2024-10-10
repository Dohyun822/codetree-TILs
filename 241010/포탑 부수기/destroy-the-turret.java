import java.util.*;
import java.io.*;

public class Main {
    private static int N;
    private static int M;
    private static int K;
    private static int[][] map;
    private static int[][] attackTime;
    private static Tower attacker;
    private static Tower strong;

    private static int[] dr = {0, 1, 0, -1}; // 우, 하, 좌, 상
    private static int[] dc = {1, 0, -1, 0};

    private static int[] ddr = {-1, -1, -1, 0, 0, 1, 1, 1}; // 포탄 공격 범위 (8방향)
    private static int[] ddc = {-1, 0, 1, -1, 1, -1, 0, 1};
    private static int[][] chk;

    private static class Tower {
        int r;
        int c;

        Tower() {
        }

        Tower(int r, int c) {
            this.r = r;
            this.c = c;
        }

        public int getR() {
            return r;
        }

        public int getC() {
            return c;
        }
    }

    public static void main(String[] args) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = new StringTokenizer(br.readLine(), " ");

        N = Integer.parseInt(st.nextToken());
        M = Integer.parseInt(st.nextToken());
        K = Integer.parseInt(st.nextToken());

        map = new int[N + 1][M + 1];
        attackTime = new int[N + 1][M + 1];

        for (int i = 1; i <= N; i++) {
            st = new StringTokenizer(br.readLine(), " ");
            for (int j = 1; j <= M; j++) {
                map[i][j] = Integer.parseInt(st.nextToken());
            }
        }

        for (int i = 1; i <= K; i++) {
            chk = new int[N + 1][M + 1]; // 이번 시간에 공격과 관련 있는 자리는 1로 설정
            setAttacker();
            attack(i);
            if (last()) {
                break;
            }
            broken();
            maintain();
        }

        answer();
    }

    private static boolean last() {
        int cnt = 0;

        for (int i = 1; i <= N; i++) {
            for (int j = 1; j <= M; j++) {
                if (map[i][j] > 0) {
                    cnt++;
                }
            }
        }

        return cnt == 1;
    }

    private static void answer() {
        int answer = 0;
        for (int i = 1; i <= N; i++) {
            for (int j = 1; j <= M; j++) {
                if (map[i][j] > answer) {
                    answer = map[i][j];
                }
            }
        }

        System.out.println(answer);
    }

    private static void maintain() {
        for (int i = 1; i <= N; i++) {
            for (int j = 1; j <= M; j++) {
                if (map[i][j] > 0 && chk[i][j] == 0) {
                    map[i][j]++;
                }
            }
        }
    }

    private static void broken() {
        for (int i = 1; i <= N; i++) {
            for (int j = 1; j <= M; j++) {
                if (map[i][j] <= 0) {
                    map[i][j] = 0;
                }
            }
        }
    }

    private static void attack(int time) {
        findTower(); // 가장 강한 포탑 찾기

        boolean laserSuccess = !raser(); // 레이저 공격 시도 후 성공 여부 반환

        if (!laserSuccess) { // 레이저 실패 시 포탄 공격
            potan();
        }

        attackTime[attacker.getR()][attacker.getC()] = time;
    }

    private static void potan() {
        int r = strong.getR();
        int c = strong.getC();
        int power = map[attacker.getR()][attacker.getC()];

        map[r][c] -= power;
        chk[r][c] = 1;

        for (int i = 0; i < 8; i++) {
            int nr = r + ddr[i];
            int nc = c + ddc[i];

            // 경계를 넘는 경우 반대편으로 이동
            if (nr < 1) {
                nr = N;
            }
            if (nr > N) {
                nr = 1;
            }
            if (nc < 1) {
                nc = M;
            }
            if (nc > M) {
                nc = 1;
            }

            // 공격자와 위치가 같지 않은 경우에만 피해 입히기
            if (!(nr == attacker.getR() && nc == attacker.getC()) && map[nr][nc] > 0) {
                chk[nr][nc] = 1;
                map[nr][nc] -= power / 2;
            }
        }
    }

    private static boolean raser() {
        Queue<int[]> queue = new ArrayDeque<>();
        boolean[][] visited = new boolean[N + 1][M + 1]; // 방문 체크 배열
        int[][][] prev = new int[N + 1][M + 1][2]; // 이전 좌표를 저장할 배열

        queue.add(new int[]{attacker.getR(), attacker.getC()});
        visited[attacker.getR()][attacker.getC()] = true;

        while (!queue.isEmpty()) {
            int[] temp = queue.poll();
            int r = temp[0];
            int c = temp[1];

            // 공격 대상 포탑에 도달하면 경로 추적 시작
            if (r == strong.getR() && c == strong.getC()) {
                tracePath(prev, r, c); // 경로 추적 함수 호출
                return false; // 레이저 공격 성공
            }

            // 4방향 탐색
            for (int i = 0; i < 4; i++) {
                int nr = r + dr[i];
                int nc = c + dc[i];

                // 경계를 넘는 경우 반대편으로 이동
                if (nr < 1) {
                    nr = N;
                }
                if (nr > N) {
                    nr = 1;
                }
                if (nc < 1) {
                    nc = M;
                }
                if (nc > M) {
                    nc = 1;
                }

                // 부서진 포탑(공격력 0인 포탑)이나 이미 방문한 곳은 건너뛰기
                if (map[nr][nc] <= 0 || visited[nr][nc]) continue;

                prev[nr][nc][0] = r;
                prev[nr][nc][1] = c;

                visited[nr][nc] = true;
                queue.add(new int[]{nr, nc});
            }
        }

        return true; // 레이저 공격 실패
    }

    private static void tracePath(int[][][] prev, int r, int c) {
        List<int[]> path = new ArrayList<>();

        int power = map[attacker.getR()][attacker.getC()];

        // 경로 추적: 공격 대상 포탑부터 출발하여 공격자의 위치까지 추적
        while (r != attacker.getR() || c != attacker.getC()) {
            path.add(new int[]{r, c});
            int tempR = prev[r][c][0];
            int tempC = prev[r][c][1];
            r = tempR;
            c = tempC;
        }

        // 경로 상의 포탑들에게 피해 입히기
        for (int[] pos : path) {
            int targetR = pos[0];
            int targetC = pos[1];

            if (!(targetR == strong.getR() && targetC == strong.getC())) {
                chk[targetR][targetC] = 1;
                map[targetR][targetC] -= (power / 2);
            }
        }

        // 공격 대상 포탑에 전체 공격력 피해 입히기
        map[strong.getR()][strong.getC()] -= power;
    }

    private static void findTower() {
        int power = 0;
        strong = new Tower(0, 0);

        for (int i = 1; i <= N; i++) {
            for (int j = 1; j <= M; j++) {
                if (map[i][j] > power) {
                    power = map[i][j];
                    strong = new Tower(i, j);
                } else if (map[i][j] == power) {
                    boolean shouldUpdate = false;

                    if (attackTime[i][j] < attackTime[strong.getR()][strong.getC()]) {
                        shouldUpdate = true;
                    } else if (attackTime[i][j] == attackTime[strong.getR()][strong.getC()]) {
                        int currentSum = i + j;
                        int strongSum = strong.getR() + strong.getC();

                        if (currentSum < strongSum) {
                            shouldUpdate = true;
                        } else if (currentSum == strongSum && j < strong.getC()) {
                            shouldUpdate = true;
                        }
                    }

                    if (shouldUpdate) {
                        strong = new Tower(i, j);
                    }
                }
            }
        }

        chk[strong.getR()][strong.getC()] = 1;
    }

    private static void setAttacker() {
        int power = 5001;

        attacker = new Tower(0, 0);

        for (int i = 1; i <= N; i++) {
            for (int j = 1; j <= M; j++) {
                if (map[i][j] < power && map[i][j] > 0) {
                    power = map[i][j];
                    attacker = new Tower(i, j);
                } else if (map[i][j] == power) {
                    boolean shouldUpdate = false;

                    if (attackTime[i][j] > attackTime[attacker.getR()][attacker.getC()]) {
                        shouldUpdate = true;
                    } else if (attackTime[i][j] == attackTime[attacker.getR()][attacker.getC()]) {
                        int currentSum = i + j;
                        int attackerSum = attacker.getR() + attacker.getC();

                        if (currentSum > attackerSum) {
                            shouldUpdate = true;
                        } else if (currentSum == attackerSum && j > attacker.getC()) {
                            shouldUpdate = true;
                        }
                    }

                    if (shouldUpdate) {
                        attacker = new Tower(i, j);
                    }
                }
            }
        }

        chk[attacker.getR()][attacker.getC()] = 1;
        map[attacker.getR()][attacker.getC()] += (N + M); // 공격력 증가
    }
}