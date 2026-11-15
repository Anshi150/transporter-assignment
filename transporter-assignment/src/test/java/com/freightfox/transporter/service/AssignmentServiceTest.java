
package com.freightfox.transporter.service;

import com.freightfox.transporter.dto.request.AssignmentRequest;
import com.freightfox.transporter.dto.response.AssignmentResponse;
import com.freightfox.transporter.entity.Lane;
import com.freightfox.transporter.entity.LaneQuote;
import com.freightfox.transporter.entity.Transporter;
import com.freightfox.transporter.repository.LaneQuoteRepository;
import com.freightfox.transporter.repository.LaneRepository;
import com.freightfox.transporter.repository.TransporterRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssignmentServiceTest {

    @Mock
    private LaneRepository laneRepository;

    @Mock
    private TransporterRepository transporterRepository;

    @Mock
    private LaneQuoteRepository laneQuoteRepository;

    @InjectMocks
    private AssignmentService assignmentService;

    private Lane lane1;
    private Lane lane2;

    private Transporter transporter1;
    private Transporter transporter2;
    private Transporter transporter3;

    @BeforeEach
    void setUp() {
        lane1 = new Lane(1L, "Indore", "Bhopal");
        lane2 = new Lane(2L, "Bhopal", "Delhi");

        transporter1 = new Transporter(1L, "Transporter A");
        transporter2 = new Transporter(2L, "Transporter B");
        transporter3 = new Transporter(3L, "Transporter C");
    }

    @Test
    void shouldReturnMinimumCostAssignment() {

        when(laneRepository.findAll())
                .thenReturn(List.of(lane1, lane2));

        when(transporterRepository.findAll())
                .thenReturn(List.of(
                        transporter1,
                        transporter2,
                        transporter3
                ));

        when(laneQuoteRepository.findAll())
                .thenReturn(List.of(
                        new LaneQuote(lane1, transporter1, new BigDecimal("10")),
                        new LaneQuote(lane1, transporter2, new BigDecimal("20")),
                        new LaneQuote(lane2, transporter1, new BigDecimal("10")),
                        new LaneQuote(lane2, transporter2, new BigDecimal("20")),
                        new LaneQuote(lane2, transporter3, new BigDecimal("1"))
                ));

        AssignmentRequest request = new AssignmentRequest();
        request.setMaxTransporters(2);

        AssignmentResponse response =
                assignmentService.assignTransporters(request);

        assertEquals("success", response.getStatus());
        assertEquals(new BigDecimal("11"), response.getTotalCost());
        assertEquals(2, response.getAssignments().size());

        assertEquals(
                List.of(1L, 3L),
                response.getSelectedTransporters()
        );

        assertEquals(1L, response.getAssignments().get(0).getLaneId());
        assertEquals(1L, response.getAssignments().get(0).getTransporterId());

        assertEquals(2L, response.getAssignments().get(1).getLaneId());
        assertEquals(3L, response.getAssignments().get(1).getTransporterId());
    }

    @Test
    void shouldThrowExceptionWhenMaxTransportersIsZero() {

        AssignmentRequest request = new AssignmentRequest();
        request.setMaxTransporters(0);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> assignmentService.assignTransporters(request)
        );

        assertEquals(
                "maxTransporters must be greater than zero.",
                exception.getMessage()
        );

        verifyNoInteractions(
                laneRepository,
                transporterRepository,
                laneQuoteRepository
        );
    }

    @Test
    void shouldThrowExceptionWhenNoLanesExist() {

        when(laneRepository.findAll()).thenReturn(List.of());
        when(transporterRepository.findAll())
                .thenReturn(List.of(transporter1));
        when(laneQuoteRepository.findAll()).thenReturn(List.of());

        AssignmentRequest request = new AssignmentRequest();
        request.setMaxTransporters(2);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> assignmentService.assignTransporters(request)
        );

        assertEquals(
                "No lanes found. Please submit input data first.",
                exception.getMessage()
        );
    }

    @Test
    void shouldThrowExceptionWhenNoTransportersExist() {

        when(laneRepository.findAll()).thenReturn(List.of(lane1));
        when(transporterRepository.findAll()).thenReturn(List.of());
        when(laneQuoteRepository.findAll()).thenReturn(List.of());

        AssignmentRequest request = new AssignmentRequest();
        request.setMaxTransporters(2);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> assignmentService.assignTransporters(request)
        );

        assertEquals(
                "No transporters found. Please submit input data first.",
                exception.getMessage()
        );
    }

    @Test
    void shouldThrowExceptionWhenLaneHasNoAvailableQuote() {

        when(laneRepository.findAll())
                .thenReturn(List.of(lane1, lane2));

        when(transporterRepository.findAll())
                .thenReturn(List.of(transporter1));

        when(laneQuoteRepository.findAll())
                .thenReturn(List.of(
                        new LaneQuote(
                                lane1,
                                transporter1,
                                new BigDecimal("100")
                        )
                ));

        AssignmentRequest request = new AssignmentRequest();
        request.setMaxTransporters(1);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> assignmentService.assignTransporters(request)
        );

        assertTrue(
                exception.getMessage().contains(
                        "No feasible assignment exists for lane ID: 2"
                )
        );
    }
}
