package com.android.zemaillib;

import android.os.AsyncTask;
import android.text.TextUtils;

import com.android.zemaillib.bean.ZEmailBean;

import java.net.URL;
import java.util.Date;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.activation.URLDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

/**
 * Created by zhengshaorui
 * Time on 2018/12/22
 */

public class ZemailRequest {
    private static final String TAG = "ZemailRequest";
    public ZemailRequest(ZEmailBean bean){
        new SendSyncTask(bean).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }



    static class SendSyncTask extends AsyncTask<Void,Void ,Boolean>{
        ZEmailBean bean;
        String errorMsg;
        private SendSyncTask(ZEmailBean bean){
            this.bean = bean;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (bean.listener != null) {
                bean.listener.sendStart();
            }
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                String host ;
                if (!TextUtils.isEmpty(bean.host)){
                    host = bean.host;
                }else {
                    host = new StringBuilder().append("smtp.")
                            .append(bean.fromAddr.split("\\@")[1].split("\\.")[0])
                            .append(".com").toString();
                }
                String port ;
                if (!TextUtils.isEmpty(bean.port)){
                    port = bean.port;
                }else{
                    if (bean.isSSLverify) {
                        port = "465";
                    }else{
                        port = "25";
                    }
                }
                Properties props = new Properties();
                props.put("mail.smtp.host", host);
                if (bean.isSSLverify) {
                    props.put("mail.smtp.socketFactory.port", port);
                    props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                    props.put("mail.smtp.auth", "true");
                    props.put("mail.smtp.port", port);
                }

                Session session ;
                if(bean.isSSLverify){
                    session = Session.getInstance(props,
                            new MailAuthenticator(bean.fromAddr,bean.password));
                }else{
                    session = Session.getInstance(props,null);
                }
                MimeMessage mimeMessage = new MimeMessage(session);
                session.setDebug(true);
                //??????????????????
                mimeMessage.setFrom(new InternetAddress(bean.fromAddr,bean.nickName,"UTF-8"));
                //????????????Email???????????????
                int count = bean.toAddrs.length;
                InternetAddress[] internetAddresses = new InternetAddress[count];
                for (int i = 0; i < count; i++) {
                    internetAddresses[i] = new InternetAddress(bean.toAddrs[i]);
                }
                mimeMessage.addRecipients(Message.RecipientType.TO,internetAddresses);

                MimeBodyPart mimeBodyPart = new MimeBodyPart();
                //???????????????????????????
                mimeBodyPart.setContent(bean.content, "text/html;charset=utf-8");
                Multipart multipart = new MimeMultipart();
                multipart.addBodyPart(mimeBodyPart);

                // ??????

                if (!TextUtils.isEmpty(bean.url)){
                    BodyPart part = new MimeBodyPart();
                    URL url = new URL(bean.url);
                    DataSource urlSource = new URLDataSource(url);
                    DataHandler dataHandler = new DataHandler(urlSource);
                    // ???????????????????????????BodyPart
                    part.setDataHandler(dataHandler);
                    // ???????????????????????????BodyPart
                    part.setFileName(MimeUtility.encodeText(urlSource.getName()));
                    multipart.addBodyPart(part);
                }
                int leng = bean.filePaths == null ? 0 :  bean.filePaths.length;
                for (int i = 0; i < leng; i++) {
                    // ??????????????????????????????
                    BodyPart part = new MimeBodyPart();
                    DataSource fileSource = new FileDataSource(bean.filePaths[i]);
                    DataHandler dataHandler = new DataHandler(fileSource);
                    // ???????????????????????????BodyPart
                    part.setDataHandler(dataHandler);
                    // ???????????????????????????BodyPart
                    part.setFileName(MimeUtility.encodeText(fileSource.getName()));
                    multipart.addBodyPart(part);
                }

                //??????
                mimeMessage.setSubject(bean.subject);
                mimeMessage.setSentDate(new Date());
                mimeMessage.setContent(multipart);

                Transport.send(mimeMessage,bean.fromAddr,bean.password);
            } catch (Exception e) {
                e.printStackTrace();
                errorMsg = e.toString();
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (aBoolean){
                if (bean.listener != null) {
                    bean.listener.sendSuccess();
                }
            }else{
                if (bean.listener != null) {
                    bean.listener.sendFailed(errorMsg);
                }
            }
        }
    }
}
