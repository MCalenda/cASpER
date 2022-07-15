package it.unisa.casper.refactor;

import it.unisa.casper.refactor.splitting_algorithm.SplitClasses;
import it.unisa.casper.refactor.splitting_algorithm.game_theory.GameTheorySplitClasses;
import it.unisa.casper.refactor.splitting_algorithm.game_theory.InputFinder;
import it.unisa.casper.refactor.splitting_algorithm.game_theory.PayoffMatrix;
import it.unisa.casper.storage.beans.*;
import org.junit.Before;
import org.junit.Test;

import java.util.*;
import java.util.logging.Logger;

import static org.junit.Assert.*;

public class GameTheorySplitClassesTest {

    private MethodBeanList methods, called1, called2, called3, called4;
    private MethodBean metodo;
    private ClassBean classe, noSmelly, smelly;
    private ClassBeanList classes;
    private PackageBean pack;
    private InputFinder inputFinder;

    @Before
    public void setUp() throws Exception {
        inputFinder = new InputFinder();
        MethodBeanList vuota = new MethodList();
        HashMap<String, ClassBean> nulla = new HashMap<String, ClassBean>();
        classes = new ClassList();
        pack = new PackageBean.Builder("blob.package", "public class BankAccount {\n" +
                "\n" +
                "    private double balance;\n" +
                "\n" +
                "    public BankAccount(double balance, int accountNumber) {\n" +
                "        this.balance = balance;\n" +
                "        this.accountNumber = accountNumber;\n" +
                "    }\n" +
                "\n" +
                "    public double withdraw(String b) {\n" +
                "            BankAccount new= BankAccount(b);\n" +
                "            b.getBalance() - 1000;\n" +
                "            return new;\n" +
                "        }" +
                "\n" +
                "}" +
                "public class Cliente {\n" +
                "\n" +
                "\tprivate String name;\n" +
                "\tprivate int età;\n" +
                "\t\n" +
                "\tpublic Cliente(String name, int età) {\n" +
                "\t\tthis.name = name;\n" +
                "\t\tthis.età = età;\n" +
                "\t}\n" +
                "\tpublic String getName() {\n" +
                "\t\treturn name;\n" +
                "\t}\n" +
                "\tpublic int getEtà() {\n" +
                "\t\treturn età;\n" +
                "\t}\n" +
                "\t\n" +
                "}\n" +
                "public class Phone {\n" +
                "   private final String unformattedNumber;\n" +
                "   public Phone(String unformattedNumber) {\n" +
                "      this.unformattedNumber = unformattedNumber;\n" +
                "   }\n" +
                "   public String getAreaCode() {\n" +
                "      return unformattedNumber.substring(0,3);\n" +
                "   }\n" +
                "   public String getPrefix() {\n" +
                "      return unformattedNumber.substring(3,6);\n" +
                "   }\n" +
                "   public String getNumber() {\n" +
                "      return unformattedNumber.substring(6,10);\n" +
                "   }\n" +
                "}" +
                "public class Ristorante {\n" +
                "\n" +
                "\tpublic String nome_Ristorante;\n" +
                "\n" +
                "\tpublic Ristorante(String nome_Ristorante) {\n" +
                "\t\tthis.nome_Ristorante = nome_Ristorante;\n" +
                "\t}\n" +
                "\n" +
                "\tpublic String getNome_Ristorante() {\n" +
                "\t\treturn nome_Ristorante;\n" +
                "\t}\n" +
                "\n" +
                "\tpublic void setNome_Ristorante(String nome_Ristorante) {\n" +
                "\t\tthis.nome_Ristorante = nome_Ristorante;\n" +
                "\t}\n" +
                "\n" +
                "}" +
                "import java.util.ArrayList;\n" +
                "import java.util.Scanner;\n" +
                "\n" +
                "public class Prodotto {\n" +
                "\n" +
                "\tpublic String uno;\n" +
                "\tpublic String due;\n" +
                "\tpublic double tre;\n" +
                "\n" +
                "    public double withdraw(String b) {\n" +
                "            BankAccount new= BankAccount(b);\n" +
                "            b.getBalance() - 1000;\n" +
                "            return new;\n" +
                "        }" +
                "\n" +
                "    public String getMobilePhoneNumber(Phone mobilePhone) {\n" +
                "          return \"(\" +\n" +
                "             mobilePhone.getAreaCode() + \") \" +\n" +
                "             mobilePhone.getPrefix() + \"-\" +\n" +
                "             mobilePhone.getNumber();\n" +
                "       }\n" +
                "\n" +
                "\tpublic String nuovoNomeRistorante() {\n" +
                "\t\tScanner in= new Scanner(System.in);\n" +
                "\t\tString ristorante=in.nextLine();\n" +
                "\t\tRistorante r= new Ristorante(ristorante);\n" +
                "\t\treturn ristorante=r.getNome_Ristorante();\n" +
                "\t}\n" +
                "\n" +
                "\n" +
                "\n" +
                "\tpublic Cliente scorriListaClienti() {\n" +
                "\t\t\n" +
                "\t\tArrayList<Cliente> clienti= new ArrayList<Cliente>();\n" +
                "\t\tCliente c= new Cliente(\"Lucia\",30);\n" +
                "\t\tclienti.add(c);\n" +
                "\t\tc= new Cliente(\"Ugo\",51);\n" +
                "\t\tclienti.add(c);\n" +
                "\t\tc= new Cliente(\"Maria\",16);\n" +
                "\t\tclienti.add(c);\n" +
                "\t\tc= new Cliente(\"Lucia\",20);\n" +
                "\t\tclienti.add(c);\n" +
                "\n" +
                "\t\tint contatore=0;\n" +
                "\n" +
                "\t\tfor(int i=0;i<4;i++) {\n" +
                "\t\t\tif(clienti.get(contatore)<clienti.get(i).getEtà()){contatore=i;}\n" +
                "\t\t}\t\n" +
                "\t\treturn clienti.get(contatore);\n" +
                "\t}\n" +
                "}\n").setClassList(classes).build();

        InstanceVariableBeanList instances = new InstanceVariableList();
        instances.getList().add(new InstanceVariableBean("uno", "String", "", "private "));
        instances.getList().add(new InstanceVariableBean("due", "String", "", "private "));
        instances.getList().add(new InstanceVariableBean("tre", "String", "", "private "));
        List<String> imports = new ArrayList<String>();
        imports.add("import java.util.Scanner;");
        imports.add("import java.util.ArrayList;");

        called1 = new MethodList();
        called2 = new MethodList();
        called3 = new MethodList();
        called4 = new MethodList();

        methods = new MethodList();
        InstanceVariableBeanList instancesBank = new InstanceVariableList();
        instancesBank.getList().add(new InstanceVariableBean("balance", "double", "", "private "));
        noSmelly = new ClassBean.Builder("blob.package.BankAccount", "private double balance;\n" +
                "\n" +
                "    public BankAccount(double balance) {\n" +
                "        this.balance = balance;\n" +
                "    }\n" +
                "\n" +
                "    public double getBalance() {\n" +
                "        return balance;\n" +
                "    }")
                .setInstanceVariables(instancesBank)
                .setMethods(methods)
                .setImports(new ArrayList<String>())
                .setLOC(10)
                .setSuperclass(null)
                .setBelongingPackage(new PackageBean.Builder("blob.package", "public class BankAccount {\n" +
                        "\n" +
                        "    private double balance;\n" +
                        "\n" +
                        "    public BankAccount(double balance, int accountNumber) {\n" +
                        "        this.balance = balance;\n" +
                        "        this.accountNumber = accountNumber;\n" +
                        "    }\n" +
                        "\n" +
                        "    public double withdraw(String b) {\n" +
                        "            BankAccount new= BankAccount(b);\n" +
                        "            b.getBalance() - 1000;\n" +
                        "            return new;\n" +
                        "        }" +
                        "\n" +
                        "}" +
                        "public class Cliente {\n" +
                        "\n" +
                        "\tprivate String name;\n" +
                        "\tprivate int età;\n" +
                        "\t\n" +
                        "\tpublic Cliente(String name, int età) {\n" +
                        "\t\tthis.name = name;\n" +
                        "\t\tthis.età = età;\n" +
                        "\t}\n" +
                        "\tpublic String getName() {\n" +
                        "\t\treturn name;\n" +
                        "\t}\n" +
                        "\tpublic int getEtà() {\n" +
                        "\t\treturn età;\n" +
                        "\t}\n" +
                        "\t\n" +
                        "}\n" +
                        "public class Phone {\n" +
                        "   private final String unformattedNumber;\n" +
                        "   public Phone(String unformattedNumber) {\n" +
                        "      this.unformattedNumber = unformattedNumber;\n" +
                        "   }\n" +
                        "   public String getAreaCode() {\n" +
                        "      return unformattedNumber.substring(0,3);\n" +
                        "   }\n" +
                        "   public String getPrefix() {\n" +
                        "      return unformattedNumber.substring(3,6);\n" +
                        "   }\n" +
                        "   public String getNumber() {\n" +
                        "      return unformattedNumber.substring(6,10);\n" +
                        "   }\n" +
                        "}" +
                        "public class Ristorante {\n" +
                        "\n" +
                        "\tpublic String nome_Ristorante;\n" +
                        "\n" +
                        "\tpublic Ristorante(String nome_Ristorante) {\n" +
                        "\t\tthis.nome_Ristorante = nome_Ristorante;\n" +
                        "\t}\n" +
                        "\n" +
                        "\tpublic String getNome_Ristorante() {\n" +
                        "\t\treturn nome_Ristorante;\n" +
                        "\t}\n" +
                        "\n" +
                        "\tpublic void setNome_Ristorante(String nome_Ristorante) {\n" +
                        "\t\tthis.nome_Ristorante = nome_Ristorante;\n" +
                        "\t}\n" +
                        "\n" +
                        "}" +
                        "import java.util.ArrayList;\n" +
                        "import java.util.Scanner;\n" +
                        "\n" +
                        "public class Prodotto {\n" +
                        "\n" +
                        "\tpublic String uno;\n" +
                        "\tpublic String due;\n" +
                        "\tpublic double tre;\n" +
                        "\n" +
                        "    public double withdraw(String b) {\n" +
                        "            BankAccount new= BankAccount(b);\n" +
                        "            b.getBalance() - 1000;\n" +
                        "            return new;\n" +
                        "        }" +
                        "\n" +
                        "    public String getMobilePhoneNumber(Phone mobilePhone) {\n" +
                        "          return \"(\" +\n" +
                        "             mobilePhone.getAreaCode() + \") \" +\n" +
                        "             mobilePhone.getPrefix() + \"-\" +\n" +
                        "             mobilePhone.getNumber();\n" +
                        "       }\n" +
                        "\n" +
                        "\tpublic String nuovoNomeRistorante() {\n" +
                        "\t\tScanner in= new Scanner(System.in);\n" +
                        "\t\tString ristorante=in.nextLine();\n" +
                        "\t\tRistorante r= new Ristorante(ristorante);\n" +
                        "\t\treturn ristorante=r.getNome_Ristorante();\n" +
                        "\t}\n" +
                        "\n" +
                        "\n" +
                        "\n" +
                        "\tpublic Cliente scorriListaClienti() {\n" +
                        "\t\t\n" +
                        "\t\tArrayList<Cliente> clienti= new ArrayList<Cliente>();\n" +
                        "\t\tCliente c= new Cliente(\"Lucia\",30);\n" +
                        "\t\tclienti.add(c);\n" +
                        "\t\tc= new Cliente(\"Ugo\",51);\n" +
                        "\t\tclienti.add(c);\n" +
                        "\t\tc= new Cliente(\"Maria\",16);\n" +
                        "\t\tclienti.add(c);\n" +
                        "\t\tc= new Cliente(\"Lucia\",20);\n" +
                        "\t\tclienti.add(c);\n" +
                        "\n" +
                        "\t\tint contatore=0;\n" +
                        "\n" +
                        "\t\tfor(int i=0;i<4;i++) {\n" +
                        "\t\t\tif(clienti.get(contatore)<clienti.get(i).getEtà()){contatore=i;}\n" +
                        "\t\t}\t\n" +
                        "\t\treturn clienti.get(contatore);\n" +
                        "\t}\n" +
                        "}\n").build())
                .setEnviedPackage(null)
                .setEntityClassUsage(2)
                .setPathToFile("C:\\Users\\Simone\\Desktop\\IdeaProjects\\Code\\testData\\blob\\package")
                .setAffectedSmell()
                .build();

        HashMap<String, ClassBean> hash = new HashMap<String, ClassBean>();
        hash.put("balance", new ClassBean.Builder("Double", "").build());
        metodo = new MethodBean.Builder("blob.package.BankAccount.BankAccount", "this.balance = balance;")
                .setReturnType(new ClassBean.Builder("void", "").build())
                .setInstanceVariableList(instancesBank)
                .setMethodsCalls(vuota)
                .setParameters(hash)
                .setStaticMethod(false)
                .setDefaultCostructor(true)
                .setBelongingClass(new ClassBean.Builder("blob.package.BankAccount ", "private double balance;\n" +
                        "\n" +
                        "    public BankAccount(double balance) {\n" +
                        "        this.balance = balance;\n" +
                        "    }\n" +
                        "\n" +
                        "    public double getBalance() {\n" +
                        "        return balance;\n" +
                        "    }").build())
                .setVisibility("public")
                .setAffectedSmell()
                .build();
        noSmelly.addMethodBeanList(metodo);

        metodo = new MethodBean.Builder("blob.package.BankAccount.getBalance", "return balance;")
                .setReturnType(new ClassBean.Builder("Double", "").build())
                .setInstanceVariableList(instancesBank)
                .setMethodsCalls(vuota)
                .setParameters(nulla)
                .setStaticMethod(false)
                .setDefaultCostructor(false)
                .setBelongingClass(new ClassBean.Builder("blob.package.BankAccount", "private double balance;\n" +
                        "\n" +
                        "    public BankAccount(double balance) {\n" +
                        "        this.balance = balance;\n" +
                        "    }\n" +
                        "\n" +
                        "    public double getBalance() {\n" +
                        "        return balance;\n" +
                        "    }").build())
                .setVisibility("public")
                .setAffectedSmell()
                .build();
        noSmelly.addMethodBeanList(metodo);
        called4.getList().add(metodo);
        pack.addClassList(noSmelly);

        methods = new MethodList();
        classe = new ClassBean.Builder("blob.package.Phone", "private final String unformattedNumber;\n" +
                "   public Phone(String unformattedNumber) {\n" +
                "      this.unformattedNumber = unformattedNumber;\n" +
                "   }\n" +
                "   public String getAreaCode() {\n" +
                "      return unformattedNumber.substring(0,3);\n" +
                "   }\n" +
                "   public String getPrefix() {\n" +
                "      return unformattedNumber.substring(3,6);\n" +
                "   }\n" +
                "   public String getNumber() {\n" +
                "      return unformattedNumber.substring(6,10);\n" +
                "   }")
                .setInstanceVariables(instances)
                .setMethods(methods)
                .setImports(new ArrayList<String>())
                .setLOC(11)
                .setSuperclass(null)
                .setBelongingPackage(new PackageBean.Builder("blob.package", "public class BankAccount {\n" +
                        "\n" +
                        "    private double balance;\n" +
                        "\n" +
                        "    public BankAccount(double balance, int accountNumber) {\n" +
                        "        this.balance = balance;\n" +
                        "        this.accountNumber = accountNumber;\n" +
                        "    }\n" +
                        "\n" +
                        "    public double withdraw(String b) {\n" +
                        "            BankAccount new= BankAccount(b);\n" +
                        "            b.getBalance() - 1000;\n" +
                        "            return new;\n" +
                        "        }" +
                        "\n" +
                        "}" +
                        "public class Cliente {\n" +
                        "\n" +
                        "\tprivate String name;\n" +
                        "\tprivate int età;\n" +
                        "\t\n" +
                        "\tpublic Cliente(String name, int età) {\n" +
                        "\t\tthis.name = name;\n" +
                        "\t\tthis.età = età;\n" +
                        "\t}\n" +
                        "\tpublic String getName() {\n" +
                        "\t\treturn name;\n" +
                        "\t}\n" +
                        "\tpublic int getEtà() {\n" +
                        "\t\treturn età;\n" +
                        "\t}\n" +
                        "\t\n" +
                        "}\n" +
                        "public class Phone {\n" +
                        "   private final String unformattedNumber;\n" +
                        "   public Phone(String unformattedNumber) {\n" +
                        "      this.unformattedNumber = unformattedNumber;\n" +
                        "   }\n" +
                        "   public String getAreaCode() {\n" +
                        "      return unformattedNumber.substring(0,3);\n" +
                        "   }\n" +
                        "   public String getPrefix() {\n" +
                        "      return unformattedNumber.substring(3,6);\n" +
                        "   }\n" +
                        "   public String getNumber() {\n" +
                        "      return unformattedNumber.substring(6,10);\n" +
                        "   }\n" +
                        "}" +
                        "public class Ristorante {\n" +
                        "\n" +
                        "\tpublic String nome_Ristorante;\n" +
                        "\n" +
                        "\tpublic Ristorante(String nome_Ristorante) {\n" +
                        "\t\tthis.nome_Ristorante = nome_Ristorante;\n" +
                        "\t}\n" +
                        "\n" +
                        "\tpublic String getNome_Ristorante() {\n" +
                        "\t\treturn nome_Ristorante;\n" +
                        "\t}\n" +
                        "\n" +
                        "\tpublic void setNome_Ristorante(String nome_Ristorante) {\n" +
                        "\t\tthis.nome_Ristorante = nome_Ristorante;\n" +
                        "\t}\n" +
                        "\n" +
                        "}" +
                        "import java.util.ArrayList;\n" +
                        "import java.util.Scanner;\n" +
                        "\n" +
                        "public class Prodotto {\n" +
                        "\n" +
                        "\tpublic String uno;\n" +
                        "\tpublic String due;\n" +
                        "\tpublic double tre;\n" +
                        "\n" +
                        "    public double withdraw(String b) {\n" +
                        "            BankAccount new= BankAccount(b);\n" +
                        "            b.getBalance() - 1000;\n" +
                        "            return new;\n" +
                        "        }" +
                        "\n" +
                        "    public String getMobilePhoneNumber(Phone mobilePhone) {\n" +
                        "          return \"(\" +\n" +
                        "             mobilePhone.getAreaCode() + \") \" +\n" +
                        "             mobilePhone.getPrefix() + \"-\" +\n" +
                        "             mobilePhone.getNumber();\n" +
                        "       }\n" +
                        "\n" +
                        "\tpublic String nuovoNomeRistorante() {\n" +
                        "\t\tScanner in= new Scanner(System.in);\n" +
                        "\t\tString ristorante=in.nextLine();\n" +
                        "\t\tRistorante r= new Ristorante(ristorante);\n" +
                        "\t\treturn ristorante=r.getNome_Ristorante();\n" +
                        "\t}\n" +
                        "\n" +
                        "\n" +
                        "\n" +
                        "\tpublic Cliente scorriListaClienti() {\n" +
                        "\t\t\n" +
                        "\t\tArrayList<Cliente> clienti= new ArrayList<Cliente>();\n" +
                        "\t\tCliente c= new Cliente(\"Lucia\",30);\n" +
                        "\t\tclienti.add(c);\n" +
                        "\t\tc= new Cliente(\"Ugo\",51);\n" +
                        "\t\tclienti.add(c);\n" +
                        "\t\tc= new Cliente(\"Maria\",16);\n" +
                        "\t\tclienti.add(c);\n" +
                        "\t\tc= new Cliente(\"Lucia\",20);\n" +
                        "\t\tclienti.add(c);\n" +
                        "\n" +
                        "\t\tint contatore=0;\n" +
                        "\n" +
                        "\t\tfor(int i=0;i<4;i++) {\n" +
                        "\t\t\tif(clienti.get(contatore)<clienti.get(i).getEtà()){contatore=i;}\n" +
                        "\t\t}\t\n" +
                        "\t\treturn clienti.get(contatore);\n" +
                        "\t}\n" +
                        "}\n").build())
                .setEnviedPackage(null)
                .setEntityClassUsage(4)
                .setPathToFile("C:\\Users\\Simone\\Desktop\\IdeaProjects\\Code\\testData\\blob\\package")
                .setAffectedSmell()
                .build();

        hash = new HashMap<String, ClassBean>();
        hash.put("unformattedNumber", new ClassBean.Builder("String", "").build());

        metodo = new MethodBean.Builder("blob.package.Phone.Phone", "this.unformattedNumber = unformattedNumber;")
                .setReturnType(new ClassBean.Builder("void", "").build())
                .setInstanceVariableList(instances)
                .setMethodsCalls(vuota)
                .setParameters(hash)
                .setStaticMethod(false)
                .setDefaultCostructor(true)
                .setBelongingClass(new ClassBean.Builder("blob.package.Phone ", "private final String unformattedNumber;\n" +
                        "   public Phone(String unformattedNumber) {\n" +
                        "      this.unformattedNumber = unformattedNumber;\n" +
                        "   }\n" +
                        "   public String getAreaCode() {\n" +
                        "      return unformattedNumber.substring(0,3);\n" +
                        "   }\n" +
                        "   public String getPrefix() {\n" +
                        "      return unformattedNumber.substring(3,6);\n" +
                        "   }\n" +
                        "   public String getNumber() {\n" +
                        "      return unformattedNumber.substring(6,10);\n" +
                        "   }").build())
                .setVisibility("public")
                .setAffectedSmell()
                .build();
        classe.addMethodBeanList(metodo);

        metodo = new MethodBean.Builder("blob.package.Phone.getAreaCode", "return unformattedNumber.substring(0,3);")
                .setReturnType(new ClassBean.Builder("String", "").build())
                .setInstanceVariableList(instances)
                .setMethodsCalls(vuota)
                .setParameters(nulla)
                .setStaticMethod(false)
                .setDefaultCostructor(false)
                .setBelongingClass(new ClassBean.Builder("blob.package.Phone", "private final String unformattedNumber;\n" +
                        "   public Phone(String unformattedNumber) {\n" +
                        "      this.unformattedNumber = unformattedNumber;\n" +
                        "   }\n" +
                        "   public String getAreaCode() {\n" +
                        "      return unformattedNumber.substring(0,3);\n" +
                        "   }\n" +
                        "   public String getPrefix() {\n" +
                        "      return unformattedNumber.substring(3,6);\n" +
                        "   }\n" +
                        "   public String getNumber() {\n" +
                        "      return unformattedNumber.substring(6,10);\n" +
                        "   }").build())
                .setVisibility("public")
                .setAffectedSmell()
                .build();
        classe.addMethodBeanList(metodo);
        called3.getList().add(metodo);

        metodo = new MethodBean.Builder("blob.package.Phone.getPrefix", "return unformattedNumber.substring(3,6);")
                .setReturnType(new ClassBean.Builder("String", "").build())
                .setInstanceVariableList(instances)
                .setMethodsCalls(vuota)
                .setParameters(nulla)
                .setStaticMethod(false)
                .setDefaultCostructor(false)
                .setBelongingClass(new ClassBean.Builder("blob.package.Phone", "private final String unformattedNumber;\n" +
                        "   public Phone(String unformattedNumber) {\n" +
                        "      this.unformattedNumber = unformattedNumber;\n" +
                        "   }\n" +
                        "   public String getAreaCode() {\n" +
                        "      return unformattedNumber.substring(0,3);\n" +
                        "   }\n" +
                        "   public String getPrefix() {\n" +
                        "      return unformattedNumber.substring(3,6);\n" +
                        "   }\n" +
                        "   public String getNumber() {\n" +
                        "      return unformattedNumber.substring(6,10);\n" +
                        "   }").build())
                .setVisibility("public")
                .setAffectedSmell()
                .build();
        classe.addMethodBeanList(metodo);
        called3.getList().add(metodo);

        metodo = new MethodBean.Builder("blob.package.Phone.getNumber", "return unformattedNumber.substring(6,10);")
                .setReturnType(new ClassBean.Builder("String", "").build())
                .setInstanceVariableList(instances)
                .setMethodsCalls(vuota)
                .setParameters(nulla)
                .setStaticMethod(false)
                .setDefaultCostructor(false)
                .setBelongingClass(new ClassBean.Builder("blob.package.Phone", "private final String unformattedNumber;\n" +
                        "   public Phone(String unformattedNumber) {\n" +
                        "      this.unformattedNumber = unformattedNumber;\n" +
                        "   }\n" +
                        "   public String getAreaCode() {\n" +
                        "      return unformattedNumber.substring(0,3);\n" +
                        "   }\n" +
                        "   public String getPrefix() {\n" +
                        "      return unformattedNumber.substring(3,6);\n" +
                        "   }\n" +
                        "   public String getNumber() {\n" +
                        "      return unformattedNumber.substring(6,10);\n" +
                        "   }").build())
                .setVisibility("public")
                .setAffectedSmell()
                .build();
        classe.addMethodBeanList(metodo);
        called3.getList().add(metodo);
        pack.addClassList(classe);

        methods = new MethodList();
        classe = new ClassBean.Builder("blob.package.Cliente", "private String name;\n" +
                "\tprivate int età;\n" +
                "\n" +
                "\tpublic Cliente(String name, int età) {\n" +
                "\t\tthis.name = name;\n" +
                "\t\tthis.età = età;\n" +
                "\t}\n" +
                "\tpublic String getName() {\n" +
                "\t\treturn name;\n" +
                "\t}\n" +
                "\tpublic int getEtà() {\n" +
                "\t\treturn età;\n" +
                "\t}")
                .setInstanceVariables(instances)
                .setMethods(methods)
                .setImports(new ArrayList<String>())
                .setLOC(12)
                .setSuperclass(null)
                .setBelongingPackage(new PackageBean.Builder("package", "").build())
                .setEnviedPackage(null)
                .setEntityClassUsage(8)
                .setPathToFile("C:\\Users\\Simone\\Desktop\\IdeaProjects\\Code\\testData\\blob\\package\\")
                .setAffectedSmell()
                .build();

        hash = new HashMap<String, ClassBean>();
        hash.put("name", new ClassBean.Builder("String", "").build());
        hash.put("età", new ClassBean.Builder("int", "").build());
        metodo = new MethodBean.Builder("blob.package.Cliente.Cliente", "this.name = name;\n" +
                "\t\tthis.età = età;")
                .setReturnType(new ClassBean.Builder("void", "").build())
                .setInstanceVariableList(instances)
                .setMethodsCalls(vuota)
                .setParameters(hash)
                .setStaticMethod(false)
                .setDefaultCostructor(true)
                .setBelongingClass(new ClassBean.Builder("blob.package.Cliente", "private String name;\n" +
                        "\tprivate int età;\n" +
                        "\n" +
                        "\tpublic Cliente(String name, int età) {\n" +
                        "\t\tthis.name = name;\n" +
                        "\t\tthis.età = età;\n" +
                        "\t}\n" +
                        "\tpublic String getName() {\n" +
                        "\t\treturn name;\n" +
                        "\t}\n" +
                        "\tpublic int getEtà() {\n" +
                        "\t\treturn età;\n" +
                        "\t}").build())
                .setVisibility("public")
                .setAffectedSmell()
                .build();
        classe.addMethodBeanList(metodo);
        called1.getList().add(metodo);

        instances.getList().remove(new InstanceVariableBean("età", "int", "", "private "));
        metodo = new MethodBean.Builder("blob.package.Cliente.getName", "return name;")
                .setReturnType(new ClassBean.Builder("String", "").build())
                .setInstanceVariableList(instances)
                .setMethodsCalls(vuota)
                .setParameters(nulla)
                .setStaticMethod(false)
                .setDefaultCostructor(false)
                .setBelongingClass(new ClassBean.Builder("blob.package.Cliente", "private String name;\n" +
                        "\tprivate int età;\n" +
                        "\n" +
                        "\tpublic Cliente(String name, int età) {\n" +
                        "\t\tthis.name = name;\n" +
                        "\t\tthis.età = età;\n" +
                        "\t}\n" +
                        "\tpublic String getName() {\n" +
                        "\t\treturn name;\n" +
                        "\t}\n" +
                        "\tpublic int getEtà() {\n" +
                        "\t\treturn età;\n" +
                        "\t}").build())
                .setVisibility("public")
                .setAffectedSmell()
                .build();
        classe.addMethodBeanList(metodo);

        instances.getList().remove(new InstanceVariableBean("name", "String", "", "private "));
        instances.getList().add(new InstanceVariableBean("età", "int", "", "private "));
        metodo = new MethodBean.Builder("blob.package.Cliente.getEtà", "return età;")
                .setReturnType(new ClassBean.Builder("int", "").build())
                .setInstanceVariableList(instances)
                .setMethodsCalls(vuota)
                .setParameters(nulla)
                .setStaticMethod(false)
                .setDefaultCostructor(false)
                .setBelongingClass(new ClassBean.Builder("blob.package.Cliente", "private String name;\n" +
                        "\tprivate int età;\n" +
                        "\n" +
                        "\tpublic Cliente(String name, int età) {\n" +
                        "\t\tthis.name = name;\n" +
                        "\t\tthis.età = età;\n" +
                        "\t}\n" +
                        "\tpublic String getName() {\n" +
                        "\t\treturn name;\n" +
                        "\t}\n" +
                        "\tpublic int getEtà() {\n" +
                        "\t\treturn età;\n" +
                        "\t}").build())
                .setVisibility("public")
                .setAffectedSmell()
                .build();
        classe.addMethodBeanList(metodo);
        called1.getList().add(metodo);
        pack.addClassList(classe);

        instances = new InstanceVariableList();
        instances.getList().add(new InstanceVariableBean("nome_Ristorante", "String", "", "private "));
        methods = new MethodList();
        classe = new ClassBean.Builder("blob.package.Ristorante", "public String nome_Ristorante;\n" +
                "\n" +
                "\tpublic Ristorante(String nome_Ristorante) {\n" +
                "\t\tthis.nome_Ristorante = nome_Ristorante;\n" +
                "\t}\n" +
                "\n" +
                "\tpublic String getNome_Ristorante() {\n" +
                "\t\treturn nome_Ristorante;\n" +
                "\t}\n" +
                "\n" +
                "\tpublic void setNome_Ristorante(String nome_Ristorante) {\n" +
                "\t\tthis.nome_Ristorante = nome_Ristorante;\n" +
                "\t}")
                .setInstanceVariables(instances)
                .setMethods(methods)
                .setImports(new ArrayList<String>())
                .setLOC(12)
                .setSuperclass(null)
                .setBelongingPackage(new PackageBean.Builder("blob.package", "").build())
                .setEnviedPackage(null)
                .setEntityClassUsage(2)
                .setPathToFile("C:\\Users\\Simone\\Desktop\\IdeaProjects\\Code\\testData\\blob\\package\\")
                .setAffectedSmell()
                .build();

        hash = new HashMap<String, ClassBean>();
        hash.put("nome_Ristorante", new ClassBean.Builder("String", "").build());
        metodo = new MethodBean.Builder("blob.package.Ristorante.Ristorante", "this.nome_Ristorante = nome_Ristorante;")
                .setReturnType(new ClassBean.Builder("void", "").build())
                .setInstanceVariableList(instances)
                .setMethodsCalls(vuota)
                .setParameters(hash)
                .setStaticMethod(false)
                .setDefaultCostructor(true)
                .setBelongingClass(new ClassBean.Builder("blob.package.Ristorante", "").build())
                .setVisibility("public")
                .setAffectedSmell()
                .build();
        classe.addMethodBeanList(metodo);
        called2.getList().add(metodo);

        metodo = new MethodBean.Builder("blob.package.Ristorante.getNome_Ristorante", "return nome_Ristorante;")
                .setReturnType(new ClassBean.Builder("String", "").build())
                .setInstanceVariableList(instances)
                .setMethodsCalls(vuota)
                .setParameters(nulla)
                .setStaticMethod(false)
                .setDefaultCostructor(false)
                .setBelongingClass(new ClassBean.Builder("Ristorante", "public String nome_Ristorante;\n" +
                        "\n" +
                        "\tpublic Ristorante(String nome_Ristorante) {\n" +
                        "\t\tthis.nome_Ristorante = nome_Ristorante;\n" +
                        "\t}\n" +
                        "\n" +
                        "\tpublic String getNome_Ristorante() {\n" +
                        "\t\treturn nome_Ristorante;\n" +
                        "\t}\n" +
                        "\n" +
                        "\tpublic void setNome_Ristorante(String nome_Ristorante) {\n" +
                        "\t\tthis.nome_Ristorante = nome_Ristorante;\n" +
                        "\t}").build())
                .setVisibility("public")
                .setAffectedSmell()
                .build();
        classe.addMethodBeanList(metodo);
        called2.getList().add(metodo);

        instances.getList().remove(new InstanceVariableBean("name", "String", "", "private "));
        instances.getList().add(new InstanceVariableBean("età", "int", "", "private "));
        metodo = new MethodBean.Builder("blob.package.Cliente.setNome_Ristorante", "this.nome_Ristorante = nome_Ristorante;")
                .setReturnType(new ClassBean.Builder("void", "").build())
                .setInstanceVariableList(instances)
                .setMethodsCalls(vuota)
                .setParameters(hash)
                .setStaticMethod(false)
                .setDefaultCostructor(false)
                .setBelongingClass(new ClassBean.Builder("blob.package.Ristorante", "public String nome_Ristorante;\n" +
                        "\n" +
                        "\tpublic Ristorante(String nome_Ristorante) {\n" +
                        "\t\tthis.nome_Ristorante = nome_Ristorante;\n" +
                        "\t}\n" +
                        "\n" +
                        "\tpublic String getNome_Ristorante() {\n" +
                        "\t\treturn nome_Ristorante;\n" +
                        "\t}\n" +
                        "\n" +
                        "\tpublic void setNome_Ristorante(String nome_Ristorante) {\n" +
                        "\t\tthis.nome_Ristorante = nome_Ristorante;\n" +
                        "\t}").build())
                .setVisibility("public")
                .setAffectedSmell()
                .build();

        classe.addMethodBeanList(metodo);
        pack.addClassList(classe);

        methods = new MethodList();
        smelly = new ClassBean.Builder("blob.package.Prodotto", "public String uno;\n" +
                "\tpublic String due;\n" +
                "\tpublic double tre;\n" +
                "\n" +
                "    public double withdraw(String b) {\n" +
                "            BankAccount new= BankAccount(b);\n" +
                "            b.getBalance() - 1000;\n" +
                "            return new;\n" +
                "        }" +
                "\n" +
                "    public String getMobilePhoneNumber(Phone mobilePhone) {\n" +
                "          return \"(\" +\n" +
                "             mobilePhone.getAreaCode() + \") \" +\n" +
                "             mobilePhone.getPrefix() + \"-\" +\n" +
                "             mobilePhone.getNumber();\n" +
                "       }\n" +
                "\n" +
                "\tpublic String nuovoNomeRistorante() {\n" +
                "\t\tScanner in= new Scanner(System.in);\n" +
                "\t\tString ristorante=in.nextLine();\n" +
                "\t\tRistorante r= new Ristorante(ristorante);\n" +
                "\t\treturn ristorante=r.getNome_Ristorante();\n" +
                "\t}\n" +
                "\n" +
                "\n" +
                "\n" +
                "\tpublic Cliente scorriListaClienti() {\n" +
                "\t\t\n" +
                "\t\tArrayList<Cliente> clienti= new ArrayList<Cliente>();\n" +
                "\t\tCliente c= new Cliente(\"Lucia\",30);\n" +
                "\t\tclienti.add(c);\n" +
                "\t\tc= new Cliente(\"Ugo\",51);\n" +
                "\t\tclienti.add(c);\n" +
                "\t\tc= new Cliente(\"Maria\",16);\n" +
                "\t\tclienti.add(c);\n" +
                "\t\tc= new Cliente(\"Lucia\",20);\n" +
                "\t\tclienti.add(c);\n" +
                "\n" +
                "\t\tint contatore=0;\n" +
                "\n" +
                "\t\tfor(int i=0;i<4;i++) {\n" +
                "\t\t\tif(clienti.get(contatore)<clienti.get(i).getEtà()){contatore=i;}\n" +
                "\t\t}\t\n" +
                "\t\treturn clienti.get(contatore);\n" +
                "\t}")
                .setInstanceVariables(instances)
                .setMethods(methods)
                .setImports(imports)
                .setLOC(42)
                .setSuperclass(null)
                .setBelongingPackage(new PackageBean.Builder("blob.package", "").build())
                .setEnviedPackage(null)
                .setEntityClassUsage(0)
                .setPathToFile("C:\\Users\\Simone\\Desktop\\IdeaProjects\\Code\\testData\\blob\\package")
                .setAffectedSmell()
                .build();

        hash = new HashMap<String, ClassBean>();
        hash.put("b", new ClassBean.Builder("String", "").build());
        metodo = new MethodBean.Builder("blob.package.Prodotto.withdraw", "public double withdraw(String b) {\n" +
                "            BankAccount new= BankAccount(b);\n" +
                "            b.getBalance() - 1000;\n" +
                "            return new;\n" +
                "        }")
                .setReturnType(new ClassBean.Builder("BankAccount", "").build())
                .setInstanceVariableList(new InstanceVariableList())
                .setMethodsCalls(called4)
                .setParameters(hash)
                .setStaticMethod(false)
                .setDefaultCostructor(false)
                .setBelongingClass(new ClassBean.Builder("blob.package.Prodotto", "public String uno;\n" +
                        "\tpublic String due;\n" +
                        "\tpublic double tre;\n" +
                        "\n" +
                        "    public double withdraw(String b) {\n" +
                        "            BankAccount new= BankAccount(b);\n" +
                        "            b.getBalance() - 1000;\n" +
                        "            return new;\n" +
                        "        }" +
                        "\n" +
                        "    public String getMobilePhoneNumber(Phone mobilePhone) {\n" +
                        "          return \"(\" +\n" +
                        "             mobilePhone.getAreaCode() + \") \" +\n" +
                        "             mobilePhone.getPrefix() + \"-\" +\n" +
                        "             mobilePhone.getNumber();\n" +
                        "       }\n" +
                        "\n" +
                        "\tpublic String nuovoNomeRistorante() {\n" +
                        "\t\tScanner in= new Scanner(System.in);\n" +
                        "\t\tString ristorante=in.nextLine();\n" +
                        "\t\tRistorante r= new Ristorante(ristorante);\n" +
                        "\t\treturn ristorante=r.getNome_Ristorante();\n" +
                        "\t}\n" +
                        "\n" +
                        "\n" +
                        "\n" +
                        "\tpublic Cliente scorriListaClienti() {\n" +
                        "\t\t\n" +
                        "\t\tArrayList<Cliente> clienti= new ArrayList<Cliente>();\n" +
                        "\t\tCliente c= new Cliente(\"Lucia\",30);\n" +
                        "\t\tclienti.add(c);\n" +
                        "\t\tc= new Cliente(\"Ugo\",51);\n" +
                        "\t\tclienti.add(c);\n" +
                        "\t\tc= new Cliente(\"Maria\",16);\n" +
                        "\t\tclienti.add(c);\n" +
                        "\t\tc= new Cliente(\"Lucia\",20);\n" +
                        "\t\tclienti.add(c);\n" +
                        "\n" +
                        "\t\tint contatore=0;\n" +
                        "\n" +
                        "\t\tfor(int i=0;i<4;i++) {\n" +
                        "\t\t\tif(clienti.get(contatore)<clienti.get(i).getEtà()){contatore=i;}\n" +
                        "\t\t}\t\n" +
                        "\t\treturn clienti.get(contatore);\n" +
                        "\t}").build())
                .setVisibility("public")
                .setAffectedSmell()
                .build();
        smelly.addMethodBeanList(metodo);

        metodo = new MethodBean.Builder("blob.package.Prodotto.listaClienti", "Scanner in= new Scanner(System.in);\n" +
                "\t\tString ristorante=in.nextLine();\n" +
                "\t\tRistorante r= new Ristorante(ristorante);\n" +
                "\t\treturn ristorante=r.getNome_Ristorante();\n" +
                "\t")
                .setReturnType(new ClassBean.Builder("String", "").build())
                .setInstanceVariableList(new InstanceVariableList())
                .setMethodsCalls(called2)
                .setParameters(nulla)
                .setStaticMethod(false)
                .setDefaultCostructor(false)
                .setBelongingClass(new ClassBean.Builder("blob.package.Prodotto", "public String uno;\n" +
                        "\tpublic String due;\n" +
                        "\tpublic double tre;\n" +
                        "\n" +
                        "    public double withdraw(String b) {\n" +
                        "            BankAccount new= BankAccount(b);\n" +
                        "            b.getBalance() - 1000;\n" +
                        "            return new;\n" +
                        "        }" +
                        "\n" +
                        "    public String getMobilePhoneNumber(Phone mobilePhone) {\n" +
                        "          return \"(\" +\n" +
                        "             mobilePhone.getAreaCode() + \") \" +\n" +
                        "             mobilePhone.getPrefix() + \"-\" +\n" +
                        "             mobilePhone.getNumber();\n" +
                        "       }\n" +
                        "\n" +
                        "\tpublic String nuovoNomeRistorante() {\n" +
                        "\t\tScanner in= new Scanner(System.in);\n" +
                        "\t\tString ristorante=in.nextLine();\n" +
                        "\t\tRistorante r= new Ristorante(ristorante);\n" +
                        "\t\treturn ristorante=r.getNome_Ristorante();\n" +
                        "\t}\n" +
                        "\n" +
                        "\n" +
                        "\n" +
                        "\tpublic Cliente scorriListaClienti() {\n" +
                        "\t\t\n" +
                        "\t\tArrayList<Cliente> clienti= new ArrayList<Cliente>();\n" +
                        "\t\tCliente c= new Cliente(\"Lucia\",30);\n" +
                        "\t\tclienti.add(c);\n" +
                        "\t\tc= new Cliente(\"Ugo\",51);\n" +
                        "\t\tclienti.add(c);\n" +
                        "\t\tc= new Cliente(\"Maria\",16);\n" +
                        "\t\tclienti.add(c);\n" +
                        "\t\tc= new Cliente(\"Lucia\",20);\n" +
                        "\t\tclienti.add(c);\n" +
                        "\n" +
                        "\t\tint contatore=0;\n" +
                        "\n" +
                        "\t\tfor(int i=0;i<4;i++) {\n" +
                        "\t\t\tif(clienti.get(contatore)<clienti.get(i).getEtà()){contatore=i;}\n" +
                        "\t\t}\t\n" +
                        "\t\treturn clienti.get(contatore);\n" +
                        "\t}").build())
                .setVisibility("public")
                .setAffectedSmell()
                .build();
        smelly.addMethodBeanList(metodo);

        instances = new InstanceVariableList();
        instances.getList().add(new InstanceVariableBean("mobilePhone", "Phone", "", "private"));
        metodo = new MethodBean.Builder("blob.package.Prodotto.getMobilePhoneNumber", "return \"(\" +\n" +
                "         mobilePhone.getAreaCode() + \") \" +\n" +
                "         mobilePhone.getPrefix() + \"-\" +\n" +
                "         mobilePhone.getNumber();\n" +
                "   }")
                .setReturnType(new ClassBean.Builder("String", "").build())
                .setInstanceVariableList(instances)
                .setMethodsCalls(called3)
                .setParameters(nulla)
                .setStaticMethod(false)
                .setDefaultCostructor(false)
                .setBelongingClass(new ClassBean.Builder("blob.package.Prodotto ", "public String uno;\n" +
                        "\tpublic String due;\n" +
                        "\tpublic double tre;\n" +
                        "\n" +
                        "    public double withdraw(String b) {\n" +
                        "            BankAccount new= BankAccount(b);\n" +
                        "            b.getBalance() - 1000;\n" +
                        "            return new;\n" +
                        "        }" +
                        "\n" +
                        "    public String getMobilePhoneNumber(Phone mobilePhone) {\n" +
                        "          return \"(\" +\n" +
                        "             mobilePhone.getAreaCode() + \") \" +\n" +
                        "             mobilePhone.getPrefix() + \"-\" +\n" +
                        "             mobilePhone.getNumber();\n" +
                        "       }\n" +
                        "\n" +
                        "\tpublic String nuovoNomeRistorante() {\n" +
                        "\t\tScanner in= new Scanner(System.in);\n" +
                        "\t\tString ristorante=in.nextLine();\n" +
                        "\t\tRistorante r= new Ristorante(ristorante);\n" +
                        "\t\treturn ristorante=r.getNome_Ristorante();\n" +
                        "\t}\n" +
                        "\n" +
                        "\n" +
                        "\n" +
                        "\tpublic Cliente scorriListaClienti() {\n" +
                        "\t\t\n" +
                        "\t\tArrayList<Cliente> clienti= new ArrayList<Cliente>();\n" +
                        "\t\tCliente c= new Cliente(\"Lucia\",30);\n" +
                        "\t\tclienti.add(c);\n" +
                        "\t\tc= new Cliente(\"Ugo\",51);\n" +
                        "\t\tclienti.add(c);\n" +
                        "\t\tc= new Cliente(\"Maria\",16);\n" +
                        "\t\tclienti.add(c);\n" +
                        "\t\tc= new Cliente(\"Lucia\",20);\n" +
                        "\t\tclienti.add(c);\n" +
                        "\n" +
                        "\t\tint contatore=0;\n" +
                        "\n" +
                        "\t\tfor(int i=0;i<4;i++) {\n" +
                        "\t\t\tif(clienti.get(contatore)<clienti.get(i).getEtà()){contatore=i;}\n" +
                        "\t\t}\t\n" +
                        "\t\treturn clienti.get(contatore);\n" +
                        "\t}").build())
                .setVisibility("public")
                .setAffectedSmell()
                .build();
        smelly.addMethodBeanList(metodo);

        metodo = new MethodBean.Builder("blob.package.Prodotto.scorriListaClienti", "ArrayList<Cliente> clienti= new ArrayList<Cliente>();\n" +
                "\t\tCliente c= new Cliente(\"Lucia\",30);\n" +
                "\t\tclienti.add(c);\n" +
                "\t\tc= new Cliente(\"Ugo\",51);\n" +
                "\t\tclienti.add(c);\n" +
                "\t\tc= new Cliente(\"Maria\",16);\n" +
                "\t\tclienti.add(c);\n" +
                "\t\tc= new Cliente(\"Lucia\",20);\n" +
                "\t\tclienti.add(c);\n" +
                "\n" +
                "\t\tint contatore=0;\n" +
                "\n" +
                "\t\tfor(int i=0;i<4;i++) {\n" +
                "\t\t\tif(clienti.get(contatore)<clienti.get(i).getEtà()){contatore=i;}\n" +
                "\t\t}\t\n" +
                "\t\treturn clienti.get(contatore);")
                .setReturnType(new ClassBean.Builder("Cliente", "").build())
                .setInstanceVariableList(new InstanceVariableList())
                .setMethodsCalls(called1)
                .setParameters(nulla)
                .setStaticMethod(false)
                .setDefaultCostructor(false)
                .setBelongingClass(new ClassBean.Builder("blob.package.Prodotto", "public String uno;\n" +
                        "\tpublic String due;\n" +
                        "\tpublic double tre;\n" +
                        "\n" +
                        "    public double withdraw(String b) {\n" +
                        "            BankAccount new= BankAccount(b);\n" +
                        "            b.getBalance() - 1000;\n" +
                        "            return new;\n" +
                        "        }" +
                        "\n" +
                        "    public String getMobilePhoneNumber(Phone mobilePhone) {\n" +
                        "          return \"(\" +\n" +
                        "             mobilePhone.getAreaCode() + \") \" +\n" +
                        "             mobilePhone.getPrefix() + \"-\" +\n" +
                        "             mobilePhone.getNumber();\n" +
                        "       }\n" +
                        "\n" +
                        "\tpublic String nuovoNomeRistorante() {\n" +
                        "\t\tScanner in= new Scanner(System.in);\n" +
                        "\t\tString ristorante=in.nextLine();\n" +
                        "\t\tRistorante r= new Ristorante(ristorante);\n" +
                        "\t\treturn ristorante=r.getNome_Ristorante();\n" +
                        "\t}\n" +
                        "\n" +
                        "\n" +
                        "\n" +
                        "\tpublic Cliente scorriListaClienti() {\n" +
                        "\t\t\n" +
                        "\t\tArrayList<Cliente> clienti= new ArrayList<Cliente>();\n" +
                        "\t\tCliente c= new Cliente(\"Lucia\",30);\n" +
                        "\t\tclienti.add(c);\n" +
                        "\t\tc= new Cliente(\"Ugo\",51);\n" +
                        "\t\tclienti.add(c);\n" +
                        "\t\tc= new Cliente(\"Maria\",16);\n" +
                        "\t\tclienti.add(c);\n" +
                        "\t\tc= new Cliente(\"Lucia\",20);\n" +
                        "\t\tclienti.add(c);\n" +
                        "\n" +
                        "\t\tint contatore=0;\n" +
                        "\n" +
                        "\t\tfor(int i=0;i<4;i++) {\n" +
                        "\t\t\tif(clienti.get(contatore)<clienti.get(i).getEtà()){contatore=i;}\n" +
                        "\t\t}\t\n" +
                        "\t\treturn clienti.get(contatore);\n" +
                        "\t}").build())
                .setVisibility("public")
                .setAffectedSmell()
                .build();
        smelly.addMethodBeanList(metodo);
        pack.addClassList(smelly);

    }

    @Test
    public void canComputeJaccardSimilarity() {
        Logger log = Logger.getLogger(getClass().getName());

        //given
        ArrayList<String> set1 = new ArrayList<>(Arrays.asList("utente", "nome", "password", "data", "luogo", "indirizzo", "azienda"));
        ArrayList<String> set2 = new ArrayList<>(Arrays.asList("azienda", "nome", "titolo", "licenza", "fondazione", "luogo", "indirizzo", "data"));
        double expected = .5;

        //when
        double actual = inputFinder.computeJaccardSimilarity(set1, set2);

        //then
        log.info("\n" + (actual == expected));
        assertEquals(expected, actual, 0.0);
    }

    @Test
    public void canExtractCleanWords() {
        Logger log = Logger.getLogger(getClass().getName());

        //given
        String testo = "public void creaUtente(String nome, String il@Cognome, integer eta ){this.nome- := nome; sa}";
        ArrayList<String> stopWords = new ArrayList<>(Arrays.asList("public", "void", "string", "integer", "this"));
        ArrayList<String> expected = new ArrayList<>(Arrays.asList("crea", "utente", "nome", "cognome", "eta", "nome", "nome"));

        //when
        ArrayList<String> actual = inputFinder.extractCleanWords(testo, stopWords);

        //then
        log.info("\n" + (actual.equals(expected)));
        assertEquals(expected, actual);
    }

    @Test
    public void canMergeTopics() {
        Logger log = Logger.getLogger(getClass().getName());

        //given
        ArrayList<String> set1 = new ArrayList<>(Arrays.asList("utente", "nome", "password", "data", "luogo", "indirizzo", "azienda"));
        ArrayList<String> set2 = new ArrayList<>(Arrays.asList("azienda", "nome", "titolo", "licenza", "fondazione", "luogo", "indirizzo", "data"));
        ArrayList<String> set3 = new ArrayList<>(Arrays.asList("azienda", "nome", "via", "pIVA"));
        ArrayList<String> set4 = new ArrayList<>(Arrays.asList("azienda", "nome", "fondazione", "utente", "licenza", "pIVA", "data"));

        ArrayList<ArrayList<String>> topics = new ArrayList<>();
        topics.add(set1);
        topics.add(set2);
        topics.add(set3);
        topics.add(set4);

        ArrayList<ArrayList<String>> expected = new ArrayList<>();
        expected.add( new ArrayList<>(Arrays.asList("password", "titolo", "luogo", "indirizzo", "azienda", "nome", "fondazione", "utente", "licenza", "pIVA", "data")));
        expected.add( new ArrayList<>(Arrays.asList("azienda", "nome", "via", "pIVA")));

        //when
        ArrayList<ArrayList<String>> actual = inputFinder.mergeTopics(topics, .5);

        //then
        log.info("\n" + (actual.equals(expected)));
        assertEquals(expected, actual);
    }

    @Test
    public void canApplyLDA() {
        boolean errorOccured = false;
        Logger log = Logger.getLogger(getClass().getName());

        //given
        int actual = 0;
        int expected = 5;

        //when
        try {
            actual = inputFinder.extractTopics(smelly, 1, 50).size();
        } catch (Exception e) {
            errorOccured = true;
            e.printStackTrace();
        }

        //then
        log.info("\n" + (actual == expected));
        assertEquals(expected, actual);

        log.info("\nError occurred:" + errorOccured);
        assertFalse(errorOccured);
    }

    @Test
    public void canFindNashEquilibrium() {
        Logger log = Logger.getLogger(getClass().getName());

        //given
        HashMap<ArrayList<Integer>, ArrayList<Double>> totalPayoffs =  new HashMap<>();

        ArrayList<Integer> var1 = new ArrayList<>(Arrays.asList(0, 1));
        ArrayList<Double> pay1 = new ArrayList<>(Arrays.asList(0.26, 0.14));
        totalPayoffs.put(var1, pay1);
        ArrayList<Integer> var2 = new ArrayList<>(Arrays.asList(0, -1));
        ArrayList<Double> pay2 = new ArrayList<>(Arrays.asList(0.26, 0.19));
        totalPayoffs.put(var2, pay2);
        ArrayList<Integer> var3 = new ArrayList<>(Arrays.asList(1, 0));
        ArrayList<Double> pay3 = new ArrayList<>(Arrays.asList(-0.26, -0.14));
        totalPayoffs.put(var3, pay3);
        ArrayList<Integer> var4 = new ArrayList<>(Arrays.asList(1, -1));
        ArrayList<Double> pay4 = new ArrayList<>(Arrays.asList(0.0, 0.05));
        totalPayoffs.put(var4, pay4);
        ArrayList<Integer> var5 = new ArrayList<>(Arrays.asList(-1, 0));
        ArrayList<Double> pay5 = new ArrayList<>(Arrays.asList(0.25, 0.31));
        totalPayoffs.put(var5, pay5);
        ArrayList<Integer> var6 = new ArrayList<>(Arrays.asList(-1, 1));
        ArrayList<Double> pay6 = new ArrayList<>(Arrays.asList(0.50, 0.45));
        totalPayoffs.put(var6, pay6);

        ArrayList<Integer> remainingMethods = new ArrayList<>(Arrays.asList(0, 1));
        PayoffMatrix pm = new PayoffMatrix(remainingMethods, null, null, 0, 0);
        pm.setTotalPayoffs(totalPayoffs);

        HashMap<ArrayList<Integer>, ArrayList<Double>> actual = new HashMap<>();
        ArrayList<Integer> expectedOne = new ArrayList<>(Arrays.asList(0, -1));
        ArrayList<Integer> expectedTwo = new ArrayList<>(Arrays.asList(-1, 1));

        //when
        actual = pm.findNashEquilibriums();

        //then
        log.info("\n" + (2 == actual.size()));
        log.info("\n" + (actual.containsKey(expectedOne)));
        log.info("\n" + (actual.containsKey(expectedTwo)));

        assertEquals(2, actual.size());
        assertTrue(actual.containsKey(expectedOne));
        assertTrue(actual.containsKey(expectedTwo));
    }

    @Test
    public void canComputePayoffs() {
        Logger log = Logger.getLogger(getClass().getName());

        //given
        double [][] methodByMethodMatrix = new double[][]{
                {1.00, 0.70, 0.21, 0.02, 0.10, 0.00},
                {0.70, 1.00, 0.30, 0.06, 0.01, 0.03},
                {0.21, 0.30, 1.00, 0.50, 0.40, 0.22},
                {0.02, 0.06, 0.50, 1.00, 0.60, 0.30},
                {0.10, 0.01, 0.40, 0.60, 1.00, 0.80},
                {0.00, 0.03, 0.22, 0.30, 0.80, 1.00}
        };

        ArrayList<Integer> remainingMethods = new ArrayList<>(Arrays.asList(0, 2, 5));
        ArrayList<ArrayList<Integer>> playerChoices = new ArrayList<>();
        playerChoices.add(new ArrayList<>(List.of(1)));
        playerChoices.add(new ArrayList<>(List.of(4)));
        playerChoices.add(new ArrayList<>(List.of(3)));

        PayoffMatrix pm = new PayoffMatrix(remainingMethods, playerChoices, methodByMethodMatrix, 0.5, 0.4);
        ArrayList<Integer> possibleChoices = new ArrayList<>(remainingMethods);
        possibleChoices.add(-1);

        int expected = 33;

        //when
        pm.computePayoffs(new ArrayList<>(), playerChoices.size(), 0, possibleChoices);

        //then
        log.info("\n" + (pm.getTotalPayoffs().size() == expected));
        assertEquals(pm.getTotalPayoffs().size(), expected);

    }

}
