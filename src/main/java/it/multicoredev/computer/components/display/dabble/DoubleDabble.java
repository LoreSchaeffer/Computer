//package it.multicoredev.computer.components.display.dabble;
//
//import it.multicoredev.computer.components.Component;
//
//public class DoubleDabble extends Component {
//    private final Dabble dabble1 = new Dabble();
//    private final Dabble dabble2 = new Dabble();
//    private final Dabble dabble3 = new Dabble();
//    private final Dabble dabble4 = new Dabble();
//    private final Dabble dabble5 = new Dabble();
//    private final Dabble dabble6 = new Dabble();
//    private final Dabble dabble7 = new Dabble();
//
//    private boolean[] in = new boolean[8];
//    private boolean[] out = new boolean[12];
//
//    @Override
//    public void run() {
//        dabble1.in(new boolean[]{false, in[0], in[1], in[2]});
//
//        dabble2.in(new boolean[]{dabble1.out()[1], dabble1.out()[2], dabble1.out()[3], in[3]});
//
//        dabble3.in(new boolean[]{dabble2.out()[1], dabble2.out()[2], dabble2.out()[3], in[4]});
//
//        dabble4.in(new boolean[]{false, dabble1.out()[0], dabble2.out()[0], dabble3.out()[0]});
//        dabble5.in(new boolean[]{dabble3.out()[1], dabble3.out()[2], dabble3.out()[3], in[5]});
//
//        dabble6.in(new boolean[]{dabble4.out()[1], dabble4.out()[2], dabble4.out()[3], dabble5.out()[0]});
//        dabble7.in(new boolean[]{dabble5.out()[1], dabble5.out()[2], dabble5.out()[3], in[6]});
//
//        out = new boolean[]{
//                false,
//                false,
//                dabble4.out()[0],
//                dabble6.out()[0],
//                dabble6.out()[1],
//                dabble6.out()[2],
//                dabble6.out()[3],
//                dabble7.out()[0],
//                dabble7.out()[1],
//                dabble7.out()[2],
//                dabble7.out()[3],
//                in[7]
//        };
//    }
//
//    public void in(boolean[] in) {
//        this.in = in;
//        run();
//    }
//
//    public boolean[] out() {
//        return out;
//    }
//}
