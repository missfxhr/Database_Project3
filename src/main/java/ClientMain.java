public class ClientMain {
    public static void main(String[] argvs){
        WorkFlow workFlow = new WorkFlow();
        if (!workFlow.login()) return;
    }
}
