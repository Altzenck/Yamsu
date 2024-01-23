package me.altzenck.test;

import me.altzenck.yml.Yml;

import java.io.StringReader;

public class Main {
    public static void main(String[] args) {
        StringBuilder sb = new StringBuilder();
        sb.append("config:\n" +
                "  values:\n" +
                "    - name: car\n" +
                "      state: 2\n" +
                "      enable: true\n" +
                "    - name: plane\n" +
                "      state: 1\n" +
                "      enable: false\n");
        Yml ins = Yml.loadYaml(new StringReader(sb.toString()));
        Object name = ins.getSection("config").getListSection("values").get(1).getInt("state");
        System.out.println(name);
    }
}
