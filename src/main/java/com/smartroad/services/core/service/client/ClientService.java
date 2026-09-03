package com.smartroad.services.core.service.client;
import com.smartroad.services.common.exception.*;
import com.smartroad.services.core.dto.client.*;
import com.smartroad.services.core.service.organization.OrganizationService;
import com.smartroad.services.domain.entity.ClientEntity;
import com.smartroad.services.domain.repository.ClientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service public class ClientService {
 private final ClientRepository clients; private final OrganizationService organizations;
 public ClientService(ClientRepository clients, OrganizationService organizations){this.clients=clients;this.organizations=organizations;}
 @Transactional public ClientResponseDTO create(UUID user, ClientRequestDTO r) throws SmartRoadException { organizations.requireMember(user,r.organizationId()); ClientEntity c=new ClientEntity(); c.setOrganizationId(r.organizationId());apply(c,r);return map(clients.save(c)); }
 @Transactional(readOnly=true) public List<ClientResponseDTO> list(UUID user,UUID org) throws SmartRoadException {organizations.requireMember(user,org);return clients.findByOrganizationIdAndActiveTrue(org).stream().map(this::map).toList();}
 @Transactional(readOnly=true) public ClientResponseDTO get(UUID user,UUID id,UUID org)throws SmartRoadException{return map(find(user,id,org));}
 @Transactional public ClientResponseDTO update(UUID user,UUID id,ClientRequestDTO r)throws SmartRoadException{ClientEntity c=find(user,id,r.organizationId());apply(c,r);c.setModifiedBy(user);return map(clients.save(c));}
 @Transactional public void delete(UUID user,UUID id,UUID org)throws SmartRoadException{ClientEntity c=find(user,id,org);c.setActive(false);c.setModifiedBy(user);clients.save(c);}
 private ClientEntity find(UUID user,UUID id,UUID org)throws SmartRoadException{organizations.requireMember(user,org);return clients.findByIdAndOrganizationIdAndActiveTrue(id,org).orElseThrow(()->new SmartRoadException(ApplicationLayer.SERVICE_LAYER,ErrorCodeMapping.DAO_NOT_FOUND,"client.not.found"));}
 private void apply(ClientEntity c,ClientRequestDTO r){c.setName(r.name());c.setContactPerson(r.contactPerson());c.setEmail(r.email());c.setPhoneNumber(r.phoneNumber());c.setGstNumber(r.gstNumber());c.setAddress(r.address());}
 private ClientResponseDTO map(ClientEntity c){return new ClientResponseDTO(c.getId(),c.getOrganizationId(),c.getName(),c.getContactPerson(),c.getEmail(),c.getPhoneNumber(),c.getGstNumber(),c.getAddress(),Boolean.TRUE.equals(c.getActive()));}
}
