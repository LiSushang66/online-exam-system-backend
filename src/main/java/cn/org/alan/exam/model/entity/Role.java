package cn.org.alan.exam.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;

/**
 * @ Author JinXi
 * @ Version 1.0
 * @ Date 2024/4/25 14:10
 */
@TableName("t_role")
public class Role implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * ID  角色表
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer Id;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 角色编码
     */
    private String code;

    public Integer getId() {
        return Id;
    }

    public void setId(Integer id) {
        this.Id = id;
    }
    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return "Role{" +
                "Id=" + Id +
                ", roleName=" + roleName +
                ", code=" + code +
                "}";
    }

}
