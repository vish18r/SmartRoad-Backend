package com.nextenti.services.core.service.client;
import com.nextenti.services.common.exception.*;
import com.nextenti.services.core.dto.client.*;
import com.nextenti.services.core.service.organization.OrganizationService;
import com.nextenti.services.domain.entity.ClientEntity;
import com.nextenti.services.domain.repository.ClientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service public class ClientService {
 private final ClientRepository clients; private final OrganizationService organizations;
 public ClientService(ClientRepository clients, OrganizationService organizations){this.clients=clients;this.organizations=organizations;}
 @Transactional public ClientResponse create(UUID user, ClientRequest r) throws SmartRoadException { organizations.requireMember(user,r.organizationId()); ClientEntity c=new Client(); c.setId(UUID.randomUUID());c.setOrganizationId(r.organizationId());c.setCreatedBy(user);c.setModifiedBy(user);apply(c,r);return map(clients.save(c)); }
 @Transactional(readOnly=true) public List<ClientResponse> list(UUID user,UUID org) throws SmartRoadException {organizations.requireMember(user,org);return clients.findByOrganizationIdAndActiveTrue(org).stream().map(this::map).toList();}
 @Transactional(readOnly=true) public ClientResponse get(UUID user,UUID id,UUID org)throws SmartRoadException{return map(find(user,id,org));}
 @Transactional public ClientResponse update(UUID user,UUID id,ClientRequest r)throws SmartRoadException{ClientEntity c=find(user,id,r.organizationId());apply(c,r);c.setModifiedBy(user);return map(clients.save(c));}
 @Transactional public void delete(UUID user,UUID id,UUID org)throws SmartRoadException{ClientEntity c=find(user,id,org);c.setActive(false);c.setModifiedBy(user);clients.save(c);}
 private ClientEntity find(UUID user,UUID id,UUID org)throws SmartRoadException{organizations.requireMember(user,org);return clients.findByIdAndOrganizationIdAndActiveTrue(id,org).orElseThrow(()->new SmartRoadException(ApplicationLayer.SERVICE_LAYER,ErrorCodeMapping.DAO_NOT_FOUND,"client.not.found"));}
 private void apply(ClientEntity c,ClientRequest r){c.setName(r.name());c.setContactPerson(r.contactPerson());c.setEmail(r.email());c.setPhoneNumber(r.phoneNumber());c.setGstNumber(r.gstNumber());c.setAddress(r.address());}
 private ClientResponse map(ClientEntity c){return new ClientResponse(c.getId(),c.getOrganizationId(),c.getName(),c.getContactPerson(),c.getEmail(),c.getPhoneNumber(),c.getGstNumber(),c.getAddress(),Boolean.TRUE.equals(c.getActive()));}
}
