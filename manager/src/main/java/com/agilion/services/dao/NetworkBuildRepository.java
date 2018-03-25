package com.agilion.services.dao;

import com.agilion.domain.app.User;
import com.agilion.services.jobmanager.NetworkBuild;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by Alex_Lappy_486 on 3/7/18.
 *
 * This class is "expanded upon" using some Spring JPA magic
 */
public interface NetworkBuildRepository extends JpaRepository<NetworkBuild, Long>
{
}
