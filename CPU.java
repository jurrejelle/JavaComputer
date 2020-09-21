@SuppressWarnings("ALL")
public class CPU {
    // Computer memory
    // 0x00000000 -> 0x7ffffffd
    // The max memory size is 0x7ffffffd
    public byte[] memory;
    // Registers, [ [],[],[],[],[] ]
    // eax, ebx, ecx, edx, edi, esi, eip
    public long[] registers;
    // Flags: [CF,ZF,SF]
    public boolean[] flags;

    public boolean running;

    public CPU() {
        memory = new byte[0x7ffffffd];
        registers = new long[13];
        flags = new boolean[3];
        registers[Registers.esp] = 0x7fffff00;
        registers[Registers.ebp] = 0x7fffff00;
    }

    public String padLeftZeros(String inputString, int length) {
        if (inputString.length() >= length) {
            return inputString;
        }
        StringBuilder sb = new StringBuilder();
        while (sb.length() < length - inputString.length()) {
            sb.append('0');
        }
        sb.append(inputString);

        return sb.toString();
    }
    public void printMemory(int startingLocation){
        printMemory(startingLocation, 100);
    }
    public void printMemory(int startingLocation, int bytes){
        for (int i = startingLocation; i < startingLocation+bytes; i++) {
            System.out.println(String.format("0x%02X", memory[i]));
        }
    }
    public int btoi(byte inByte){
        return ((int)inByte) & 0xff;
    }
    public long binaryAdd(long a, long b) {
        String String1 = padLeftZeros(Long.toBinaryString(a), 32);
        String String2 = padLeftZeros(Long.toBinaryString(b), 32);
        char[] String1Array = String1.toCharArray();
        char[] String2Array = String2.toCharArray();
        String outString = "";
        int carry = 0;
        for (int i = 31; i > 0; i--) {
            int temp = (int) String1Array[i]-48 + (int) String2Array[i]-48 + carry;
            carry = 0;
            if (temp > 1) {
                temp = temp - 2;
                carry = 1;
            }
            outString = Integer.toString(temp) + outString;
        }
        long outputLong = Long.parseLong(outString, 2);
        flags[Flags.CF] = carry==1;
        return outputLong;
    }


    //=============== ALL THE FETCH INSTRUCTIONS FROM EIP =========================
    public long fetch32(){
        int ip = (int) registers[Registers.eip];
        long returnValue = fetch32at(ip);
        registers[Registers.eip] = ip+4;
        return returnValue;
    }
    public int fetch16(){
        int ip = (int) registers[Registers.eip];
        int returnValue = fetch16at(ip);
        registers[Registers.eip] = ip+2;
        return returnValue;

    }
    public byte fetch(){
        int ip = (int) registers[Registers.eip];
        byte returnValue = fetchat(ip);
        registers[Registers.eip] = ip+1;
        return returnValue;

    }
    //=============== ALL THE FETCH INSTRUCTIONS FROM ADDRESS =========================

    public long fetch32at(int address){
        byte byte1 = memory[address++];
        byte byte2 = memory[address++];
        byte byte3 = memory[address++];
        byte byte4 = memory[address++];
        long result = btoi(byte4) + btoi(byte3) * 0x100 + btoi(byte2) * 0x10000 + btoi(byte1) * 0x1000000;
        return result;
    }
    public int fetch16at(int address){
        byte byte1 = memory[address++];
        byte byte2 = memory[address++];
        int result = btoi(byte2) + btoi(byte1) * 0x100;
        return result;
    }
    public byte fetchat(int address){
        byte byte1 = memory[address];
        return byte1;
    }

    //=============== ALL THE WRITE INSTRUCTIONS =========================
    /**
     * Writes the 32 bit value to the address in memory
     * @param value the value to write
     * @param address the address to write to
     */
    public void write32at(long value, int address) {
        //value = 0x12345678
        byte byte4 = (byte) (value & 0x000000ff); //0x00000078
        byte byte3 = (byte) ((value & 0x0000ff00) >> 8); //0x00005600
        byte byte2 = (byte) ((value & 0x00ff0000) >> 16);
        byte byte1 = (byte) ((value & 0xff000000) >> 24);
        memory[address++] = byte1;
        memory[address++] = byte2;
        memory[address++] = byte3;
        memory[address] = byte4;
    }
    public void write16at(int value, int address){
        byte byte2 = (byte) (value & 0x00ff); //0x00000078
        byte byte1 = (byte) ((value & 0xff00) >> 8); //0x00005600
        memory[address++] = byte1;
        memory[address] = byte2;
    }
    public void write8at(byte value, int address){
        memory[address] = value;
    }
    public void push(long literal){
        registers[Registers.esp] -= 4;
        write32at(literal, (int)registers[Registers.esp]);
    }
    public long pop(){
        long returnValue = fetch32at((int)registers[Registers.esp]);
        registers[Registers.esp] += 4;
        return returnValue;
    }
    /**
     * Executes 1 instruction starting at the instruction pointer
     */
    public void step(){
        byte instruction = fetch();

        switch(instruction){
            //=====================Implementation of MOV instructions===============
            case Instructions.MOV_LIT_MEM: {
                long lit = fetch32();
                int mem = fetch16();
                write32at(lit, mem);
                break;
            }
            case Instructions.MOV_LIT_REG: {
                long lit = fetch32();
                int reg = fetch();
                registers[reg] = lit;
                break;
            }
            case Instructions.MOV_REG_REG: {
                int reg1 = fetch();
                int reg2 = fetch();
                registers[reg2] = registers[reg1];
                break;
            }
            case Instructions.MOV_REG_MEM: {
                int reg = fetch();
                int mem = fetch16();
                write32at(registers[reg],mem);
                break;
            }
            case Instructions.MOV_MEM_REG: {
                int mem = fetch16();
                int reg = fetch();
                registers[reg] = fetch32at(mem);
                break;
            }
            //=====================Implementation of Arithmetics instructions===============
            case Instructions.ADD_LIT_REG: {
                long lit = fetch32();
                int reg = fetch();
                if(registers[reg]+lit>0x100000000l)
                registers[reg] = binaryAdd(registers[reg], lit);
                break;
            }
            case Instructions.ADD_REG_REG: {
                int reg1 = fetch();
                int reg2 = fetch();
                registers[reg2] = binaryAdd(registers[reg2], registers[reg1]);
                break;
            }
            case Instructions.SUB_LIT_REG: {
                long lit = fetch32();
                int reg = fetch();
                //Set appropriate CF
                registers[reg] = registers[reg] - lit;
                flags[Flags.CF] = registers[reg]<0;
                flags[Flags.ZF] = registers[reg]==0;
                break;
            }
            case Instructions.SUB_REG_REG: {
                int reg1 = fetch();
                int reg2 = fetch();

                registers[reg2] = registers[reg2] - registers[reg1];
                flags[Flags.CF] = registers[reg2]<0;
                flags[Flags.ZF] = registers[reg2]==0;
                break;
            }
            case Instructions.MUL_LIT: {
                long lit = fetch32();
                registers[Registers.eax] = registers[Registers.eax] * lit;
                break;
            }
            case Instructions.MUL_REG: {
                int reg1 = fetch();
                registers[Registers.eax] = registers[Registers.eax] * registers[reg1];
                break;
            }
            case Instructions.DIV_LIT: {
                long lit = fetch32();
                registers[Registers.edx] = registers[Registers.eax] % lit;
                registers[Registers.eax] = registers[Registers.eax] / lit;
                break;
            }
            case Instructions.DIV_REG: {
                int reg1 = fetch();
                registers[Registers.edx] = registers[Registers.eax] % registers[reg1];
                registers[Registers.eax] = registers[Registers.eax] / registers[reg1];
                break;
            }
            //=====================Implementation of function call instructions===============
            case Instructions.CALL_PRINTF: {
                //Calling convention: rdi, rsi, rdx, rcx
                int formatstring_memorylocation = (int) registers[Registers.edi];
                byte charByte = fetchat(formatstring_memorylocation++);
                String formatString = "";
                while(charByte != 0){
                    formatString += (char) charByte;
                    charByte = fetchat(formatstring_memorylocation++);
                }
                long vararg1 = registers[Registers.esi];
                long vararg2 = registers[Registers.edx];
                long vararg3 = registers[Registers.ecx];
                System.out.printf(formatString,vararg1,vararg2,vararg3);
                break;
            }
            case Instructions.CALL_PUTCHAR: {
                //Calling convention: rdi
                char charByte = (char) (registers[Registers.edi] & 0xff);
                System.out.print(charByte);
                break;
            }
            case Instructions.CALL_EXIT: {
                //Calling convention: rdi
                running = false;
                break;
            }
            //=====================Implementation of branching instructions===============

            case Instructions.CMP_LIT_REG: {
                long lit = fetch32();
                byte reg = fetch();
                long reg_value = registers[reg];
                flags[Flags.CF] = lit-reg_value<0;
                flags[Flags.ZF] = lit-reg_value==0;
                break;
            }
            case Instructions.CMP_REG_REG: {
                byte reg1 = fetch();
                byte reg2 = fetch();
                long reg_value1 = registers[reg1];
                long reg_value2 = registers[reg2];
                flags[Flags.CF] = reg_value1-reg_value2<0;
                flags[Flags.ZF] = reg_value1-reg_value2==0;
                break;
            }
            case Instructions.JMP_LIT: {
                long address = fetch32();
                registers[Registers.eip] = address;
                break;
            }
            case Instructions.JMP_REG: {
                byte reg = fetch();
                registers[Registers.eip] = registers[reg];
                break;
            }
            case Instructions.JE_LIT: {
                long address = fetch32();
                if(flags[Flags.ZF]==true) {
                    registers[Registers.eip] = address;
                }
                break;
            }
            case Instructions.JE_REG: {
                byte reg = fetch();
                if(flags[Flags.ZF]==true) {
                    registers[Registers.eip] = registers[reg];
                }
                break;
            }
            case Instructions.JNE_LIT: {
                long address = fetch32();
                if(flags[Flags.ZF]!=true) {
                    registers[Registers.eip] = address;
                }
                break;
            }
            case Instructions.JNE_REG: {
                byte reg = fetch();
                if(flags[Flags.ZF]!=true) {
                    registers[Registers.eip] = registers[reg];
                }
                break;
            }
            case Instructions.JGE_LIT: {
                long address = fetch32();
                if(flags[Flags.ZF]==true || flags[Flags.CF]==false) {
                    registers[Registers.eip] = address;
                }
                break;
            }
            case Instructions.JGE_REG: {
                byte reg = fetch();
                if(flags[Flags.ZF]==true || flags[Flags.CF]==false) {
                    registers[Registers.eip] = registers[reg];
                }
                break;
            }
            case Instructions.JG_LIT: {
                long address = fetch32();
                if(flags[Flags.ZF]==false && flags[Flags.CF]==false) {
                    registers[Registers.eip] = address;
                }
                break;
            }
            case Instructions.JG_REG: {
                byte reg = fetch();
                if(flags[Flags.ZF]==false && flags[Flags.CF]==false) {
                    registers[Registers.eip] = registers[reg];
                }
                break;
            }
            case Instructions.JLE_LIT: {
                long address = fetch32();
                if(flags[Flags.ZF]==true || flags[Flags.CF]==true) {
                    registers[Registers.eip] = address;
                }
                break;
            }
            case Instructions.JLE_REG: {
                byte reg = fetch();
                if(flags[Flags.ZF]==true || flags[Flags.CF]==true) {
                    registers[Registers.eip] = registers[reg];
                }
                break;
            }
            case Instructions.JL_LIT: {
                long address = fetch32();
                if(flags[Flags.ZF]==false && flags[Flags.CF]==true) {
                    registers[Registers.eip] = address;
                }
                break;
            }
            case Instructions.JL_REG: {
                byte reg = fetch();
                if(flags[Flags.ZF]==false && flags[Flags.CF]==true) {
                    registers[Registers.eip] = registers[reg];
                }
                break;
            }
            //=====================================Implementation of Stack Manipulation=================

            case Instructions.PUSH_LIT: {
                long lit = fetch32();
                push(lit);
                break;
            }
            case Instructions.PUSH_REG: {
                byte reg = fetch();
                push(registers[reg]);
                break;
            }
            case Instructions.POP_REG: {
                byte reg = fetch();
                registers[reg] = pop();
                break;
            }
            case Instructions.CALL_LIT: {
                long lit = fetch32();
                //PRESERVE REGISTERS
                for(int i=0;i<registers.length;i++){
                    if(i!=Registers.eax && i!=Registers.esp && i!=Registers.ebp && i!=Registers.eip){
                        push(registers[i]);
                    }
                }

                push(registers[Registers.eip]);
                registers[Registers.eip] = lit;
                break;
            }
            case Instructions.RET: {
                long returnAddress = pop();
                registers[Registers.eip] = returnAddress;
                //RETURN REGISTERS TO ORIGINAL STATE
                for(int i=registers.length-1;i>=0;i--){
                    if(i!=Registers.eax && i!=Registers.esp && i!=Registers.ebp && i!=Registers.eip){
                        registers[i] = pop();
                    }
                }
                break;
            }
            case Instructions.ENTER: {
                //PUSH RBP
                //MOV RSP,RBP

                push(registers[Registers.ebp]);
                registers[Registers.ebp] = registers[Registers.esp];
                break;
            }
            case Instructions.LEAVE: {
                registers[Registers.esp] = registers[Registers.ebp];
                registers[Registers.ebp] = pop();
                break;
            }
            //========================Implementation of Binary Logic======================

            case Instructions.SHL_LIT_REG: {
                byte lit = fetch();
                byte reg = fetch();
                long result = (registers[reg] << lit) & 0xffffffff;
                registers[reg] = result;
                break;
            }
            case Instructions.SHL_REG_REG: {
                byte reg1 = fetch();
                byte reg2 = fetch();
                long result = (registers[reg2] << registers[reg1]) & 0xffffffff;
                registers[reg2] = result;
                break;
            }
            case Instructions.SHR_LIT_REG: {
                byte lit = fetch();
                byte reg = fetch();
                long result = (registers[reg] >> lit) & 0xffffffff;
                registers[reg] = result;
                break;
            }
            case Instructions.SHR_REG_REG: {
                byte reg1 = fetch();
                byte reg2 = fetch();
                long result = (registers[reg2] >> registers[reg1]) & 0xffffffff;
                registers[reg2] = result;
                break;
            }
            case Instructions.AND_LIT_REG: {
                byte lit = fetch();
                byte reg = fetch();
                long result = (registers[reg] & lit) & 0xffffffff;
                registers[reg] = result;
                break;
            }
            case Instructions.AND_REG_REG: {
                byte reg1 = fetch();
                byte reg2 = fetch();
                long result = (registers[reg2] & registers[reg1]) & 0xffffffff;
                registers[reg2] = result;
                break;
            }
            case Instructions.OR_LIT_REG: {
                byte lit = fetch();
                byte reg = fetch();
                long result = (registers[reg] | lit) & 0xffffffff;
                registers[reg] = result;
                break;
            }
            case Instructions.OR_REG_REG: {
                byte reg1 = fetch();
                byte reg2 = fetch();
                long result = (registers[reg2] | registers[reg1]) & 0xffffffff;
                registers[reg2] = result;
                break;
            }
            case Instructions.XOR_LIT_REG: {
                byte lit = fetch();
                byte reg = fetch();
                long result = (registers[reg] ^ lit) & 0xffffffff;
                registers[reg] = result;
                break;
            }
            case Instructions.XOR_REG_REG: {
                byte reg1 = fetch();
                byte reg2 = fetch();
                long result = (registers[reg2] ^ registers[reg1]) & 0xffffffff;
                registers[reg2] = result;
                break;
            }
            case Instructions.NOT: {
                byte reg1 = fetch();
                long result = (~registers[reg1]) & 0xffffffff;
                registers[reg1] = result;
                break;
            }


        }

    }
    public void run(){
        running = true;
        while(running){
            step();
        }
    }

    public static void main(String[] args) {
        CPU myCPU = new CPU();
        //EDI=Base
        //ESI=Exponent
        int i = 0;

        myCPU.memory[i++] = Instructions.MOV_LIT_REG;
        myCPU.memory[i++] = 0x00;
        myCPU.memory[i++] = 0x00;
        myCPU.memory[i++] = 0x00;
        myCPU.memory[i++] = 0x05;
        myCPU.memory[i++] = Registers.edi;

        myCPU.memory[i++] = Instructions.MOV_LIT_REG;
        myCPU.memory[i++] = 0x00;
        myCPU.memory[i++] = 0x00;
        myCPU.memory[i++] = 0x00;
        myCPU.memory[i++] = 0x05;
        myCPU.memory[i++] = Registers.esi;

        myCPU.memory[i++] = Instructions.MOV_LIT_REG;
        myCPU.memory[i++] = 0x00;
        myCPU.memory[i++] = 0x00;
        myCPU.memory[i++] = 0x00;
        myCPU.memory[i++] = 0x01;
        myCPU.memory[i++] = Registers.eax;

        i=0x20;
        //LOOP: 32 / 0x20
        myCPU.memory[i++] = Instructions.CMP_LIT_REG;
        myCPU.memory[i++] = 0x00;
        myCPU.memory[i++] = 0x00;
        myCPU.memory[i++] = 0x00;
        myCPU.memory[i++] = 0x00;
        myCPU.memory[i++] = Registers.esi;

        myCPU.memory[i++] = Instructions.JE_LIT;
        myCPU.memory[i++] = 0x00;
        myCPU.memory[i++] = 0x00;
        myCPU.memory[i++] = 0x04;
        myCPU.memory[i++] = 0x00;
        myCPU.memory[i++] = Registers.esi;

        myCPU.memory[i++] = Instructions.MUL_REG;
        myCPU.memory[i++] = Registers.edi;

        myCPU.memory[i++] = Instructions.SUB_LIT_REG;
        myCPU.memory[i++] = 0x00;
        myCPU.memory[i++] = 0x00;
        myCPU.memory[i++] = 0x00;
        myCPU.memory[i++] = 0x01;
        myCPU.memory[i++] = Registers.esi;

        myCPU.memory[i++] = Instructions.JMP_LIT;
        myCPU.memory[i++] = 0x00;
        myCPU.memory[i++] = 0x00;
        myCPU.memory[i++] = 0x00;
        myCPU.memory[i++] = 0x20;

//After:
        i = 0x400;
        myCPU.memory[i++] = Instructions.MOV_REG_REG;
        myCPU.memory[i++] = Registers.eax;
        myCPU.memory[i++] = Registers.esi;

        myCPU.memory[i++] = Instructions.MOV_LIT_REG;
        myCPU.memory[i++] = 0x00;
        myCPU.memory[i++] = 0x00;
        myCPU.memory[i++] = 0x10;
        myCPU.memory[i++] = 0x00;
        myCPU.memory[i++] = Registers.edi;

        myCPU.memory[i++] = Instructions.CALL_PRINTF;

        myCPU.memory[i++] = Instructions.CALL_EXIT;

        i = 0x1000;
        myCPU.memory[i++] = (char) '%';
        myCPU.memory[i++] = (char) 'd';
        myCPU.memory[i++] = 0x00;

        myCPU.run();
    }
}
