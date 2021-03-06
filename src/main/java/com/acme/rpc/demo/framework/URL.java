package com.acme.rpc.demo.framework;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author: K.
 * @date: 2021/1/15 下午6:56
 * 自定义的URL类，方便管理
 */
@Data
@AllArgsConstructor
public class URL implements Serializable {

    private static final long serialVersionUID = 7947426935048871471L;
    private String hostname;
    private Integer port;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        URL url = (URL) o;
        return Objects.equals(hostname, url.hostname) &&
                Objects.equals(port, url.port);
    }

    @Override
    public String toString() {
        return hostname + ":" + port;
    }

    @Override
    public int hashCode() {

        return Objects.hash(hostname, port);
    }
}
