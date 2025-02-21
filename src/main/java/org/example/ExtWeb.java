package org.example;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import com.opencsv.CSVWriter;

import javax.swing.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ExtWeb
{
    public static void AddCsv(String url)
    {
        try(CloseableHttpClient httpClient = HttpClients.createDefault())
        {
            String html = httpClient.execute(new HttpGet(url), response-> new String(response.getEntity().getContent().readAllBytes(),StandardCharsets.UTF_8));
            String loja, nome, precoString, pref = "https://www.";;
            Element preco = null, titulo = null;
            CSVWriter csvWriter = null;
            FileWriter prodCSV;
            File arq = null;

            if(url.startsWith(pref))
                loja = url.substring(pref.length(), url.indexOf(".",pref.length()));
            else
                loja = url.substring(url.indexOf(".", "https://".length()));

            Document doc = Jsoup.parse(html);
            doc.select("script, style, meta, link, noscript").remove();

            switch (loja)
            {
                case "mercadolivre":
                     preco = doc.selectFirst(".andes-money-amount__fraction");
                     titulo = doc.selectFirst(".ui-pdp-title");
                    break;
                case "amazon":
                     preco = doc.selectFirst("span.a-offscreen");
                     titulo = doc.selectFirst("div#titleSection");
                     break;
                case "shopee":
                    preco = doc.selectFirst(".IZPeQz");
                    titulo = doc.selectFirst(".WBVL_7");
                    break;
            }

            assert titulo != null;
            assert preco != null;

            Path path = Paths.get("produtos.csv");

            try {
                if(Files.exists(path))
                {
                    arq = path.toFile();
                    prodCSV = new FileWriter(arq, true);
                    csvWriter = new CSVWriter(prodCSV);
                }
                else
                {
                    Files.createFile(path);
                    arq = path.toFile();

                    prodCSV = new FileWriter(arq, true);
                    csvWriter = new CSVWriter(prodCSV);
                    csvWriter.writeNext(new String[] {"titulo","preco","loja","link"});
                }
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
            assert csvWriter != null;

            nome = titulo.text();
            precoString = preco.text();

            if(!arq.toString().contains(nome))
                csvWriter.writeNext(new String[] {nome, precoString, loja,url});

            csvWriter.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return;
        }
    }
}
