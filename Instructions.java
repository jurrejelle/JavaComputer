public class Instructions {
    //MOV-type instructions
    final static public byte MOV_LIT_REG = 0x10;
    final static public byte MOV_LIT_MEM = 0x11;
    final static public byte MOV_REG_REG = 0x12;
    final static public byte MOV_REG_MEM = 0x13;
    final static public byte MOV_MEM_REG = 0x14;

    //Arithmetics
    final static public byte ADD_REG_REG = 0x20;
    final static public byte ADD_LIT_REG = 0x21;
    final static public byte SUB_REG_REG = 0x22;
    final static public byte SUB_LIT_REG = 0x23;
    final static public byte MUL_REG = 0x24;
    final static public byte MUL_LIT = 0x25;
    final static public byte DIV_REG = 0x26;
    final static public byte DIV_LIT = 0x27;

    //Function calls
    final static public byte CALL_PRINTF = 0x30;
    final static public byte CALL_PUTCHAR = 0x31;
    final static public byte CALL_SCANF = 0x32;
    final static public byte CALL_EXIT = 0x33;

    //Branching
    final static public byte JMP_LIT = 0x40;
    final static public byte JMP_REG = 0x41;
    final static public byte JE_LIT = 0x42;
    final static public byte JE_REG = 0x43;
    final static public byte JNE_LIT = 0x44;
    final static public byte JNE_REG = 0x45;
    final static public byte JGE_LIT = 0x46;
    final static public byte JGE_REG = 0x47;
    final static public byte JG_LIT = 0x48;
    final static public byte JG_REG = 0x49;
    final static public byte JLE_LIT = 0x4a;
    final static public byte JLE_REG = 0x4b;
    final static public byte JL_LIT = 0x4c;
    final static public byte JL_REG = 0x4d;
    final static public byte CMP_LIT_REG = 0x4e;
    final static public byte CMP_REG_REG = 0x4f;

    //Stack manipulation
    final static public byte PUSH_LIT = 0x50;
    final static public byte PUSH_REG = 0x51;
    final static public byte POP_REG = 0x52;
    final static public byte CALL_LIT = 0x53;
    final static public byte RET = 0x54;
    final static public byte ENTER = 0x55;
    final static public byte LEAVE = 0x56;

    //Binary logic
    final static public byte SHL_REG_REG = 0x60;
    final static public byte SHL_LIT_REG = 0x61;
    final static public byte SHR_REG_REG = 0x62;
    final static public byte SHR_LIT_REG = 0x63;
    final static public byte AND_REG_REG = 0x64;
    final static public byte AND_LIT_REG = 0x65;
    final static public byte OR_REG_REG = 0x66;
    final static public byte OR_LIT_REG = 0x67;
    final static public byte XOR_REG_REG = 0x68;
    final static public byte XOR_LIT_REG = 0x69;
    final static public byte NOT = 0x6a;

}


