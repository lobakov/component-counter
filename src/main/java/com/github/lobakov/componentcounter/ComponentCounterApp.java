package com.github.lobakov.componentcounter;

import java.io.IOException;
import java.net.HttpCookie;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.TreeMap;

import com.github.lobakov.componentcounter.restclient.Authenticator;
import com.github.lobakov.componentcounter.restclient.ReportRetreiver;
import com.github.lobakov.componentcounter.util.CCFileReader;
import com.github.lobakov.componentcounter.util.CredentialParser;

public class ComponentCounterApp {

    private static final int ARGS_LENGTH = 2;
    private static final int CRED_INDEX = 0;
    private static final int FILE_INDEX = 1;
    private static final String INFO = "Использование: <логин>:<пароль>:<отп> <имя_файла>";
    private static final String LEGEND = "Где <имя_файла> - имя текстового файла со списком SAPов";
    private static final String WARNING = "Внимание! Одна строка файла == один SAP ID";
    private static final String NL = System.lineSeparator();
    private static final String DELIMITER = " ------ ";
    private static final String PCS = " шт.";

    public static void main(String[] args) throws IOException, URISyntaxException {
        if (args.length != ARGS_LENGTH) {
            StringJoiner joiner = new StringJoiner(NL);
            joiner.add("").add(INFO).add("").add(LEGEND).add(WARNING).add("");
            return;
        }

        String[] credentials = CredentialParser.parse(args[CRED_INDEX]);
        Authenticator authenticator = new Authenticator();
        HttpCookie authCookie = authenticator.authenticate(credentials);
        ReportRetreiver reportRetreiver = new ReportRetreiver(authCookie);

        String file = args[FILE_INDEX];
        List<String> sapList = CCFileReader.read(file);
        Map<String, Integer> report = new TreeMap<>(reportRetreiver.getReport(sapList));
        report.forEach((key, value) -> System.out.println(key + DELIMITER + value + PCS));
    }
}
